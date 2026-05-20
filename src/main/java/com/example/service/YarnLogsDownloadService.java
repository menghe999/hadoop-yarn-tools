/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnLogsDownloadRequest;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.logaggregation.ContainerLogsRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * YARN 聚合日志下载服务，负责加载 Hadoop 配置、按需 Kerberos 登录并调用日志拉取执行器。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Service
public class YarnLogsDownloadService {

    private static final Object KERBEROS_LOCK = new Object();

    private static final String CORE_SITE = "core-site.xml";

    private static final String HDFS_SITE = "hdfs-site.xml";

    private static final String YARN_SITE = "yarn-site.xml";

    private final YarnLogsDumper yarnLogsDumper;
    private final YarnClusterProperties yarnClusterProperties;

    public YarnLogsDownloadService(YarnLogsDumper yarnLogsDumper, YarnClusterProperties yarnClusterProperties) {
        this.yarnLogsDumper = yarnLogsDumper;
        this.yarnClusterProperties = yarnClusterProperties;
    }

    /**
     * 下载指定 YARN 应用的聚合日志到服务端临时目录，并判断后续应直接下载单文件还是打包 ZIP。
     *
     * @param request 日志下载请求参数
     * @return 已下载日志的服务端临时结果
     */
    public DownloadedYarnLogs download(YarnLogsDownloadRequest request) {
        java.nio.file.Path temporaryDirectory = null;
        try {
            temporaryDirectory = Files.createTempDirectory("yarn-logs-");
            YarnClusterProperties.Cluster cluster = yarnClusterProperties.requireCluster(request.getClusterId());
            Configuration configuration = loadHadoopConfiguration(cluster.getYarnConfigDir());
            ContainerLogsRequest containerLogsRequest = buildContainerLogsRequest(request, temporaryDirectory);
            int result = dumpWithClusterCredentials(cluster, configuration, containerLogsRequest);
            if (result != 0) {
                throw new YarnLogsDownloadException("YARN日志拉取失败，返回码: " + result);
            }
            return buildDownloadResult(request.getApplicationId(), temporaryDirectory);
        } catch (YarnLogsInvalidRequestException e) {
            cleanupOnFailure(temporaryDirectory, e);
            throw e;
        } catch (YarnLogsDownloadException e) {
            cleanupOnFailure(temporaryDirectory, e);
            throw e;
        } catch (IOException e) {
            cleanupOnFailure(temporaryDirectory, e);
            throw new YarnLogsDownloadException("YARN日志下载IO异常", e);
        } catch (IllegalArgumentException e) {
            cleanupOnFailure(temporaryDirectory, e);
            throw new YarnLogsInvalidRequestException("applicationId格式不正确");
        } catch (RuntimeException e) {
            cleanupOnFailure(temporaryDirectory, e);
            throw new YarnLogsDownloadException("YARN日志下载失败: " + e.getMessage(), e);
        }
    }

    private Configuration loadHadoopConfiguration(String yarnConfigDir) {
        Configuration configuration = new Configuration();
        configuration.addResource(new Path(yarnConfigDir + File.separator + CORE_SITE));
        configuration.addResource(new Path(yarnConfigDir + File.separator + HDFS_SITE));
        configuration.addResource(new Path(yarnConfigDir + File.separator + YARN_SITE));
        return configuration;
    }

    private int dumpWithClusterCredentials(
            YarnClusterProperties.Cluster cluster, Configuration configuration, ContainerLogsRequest request) throws IOException {
        if (!cluster.isKerberosEnabled()) {
            return yarnLogsDumper.dump(configuration, request);
        }
        synchronized (KERBEROS_LOCK) {
            loginKerberos(configuration, cluster.getPrincipal(), cluster.getKeytabPath(), cluster.getKrb5Path());
            return yarnLogsDumper.dump(configuration, request);
        }
    }

    private void loginKerberos(Configuration configuration, String principal, String keytabPath, String krb5Path)
            throws IOException {
        System.setProperty("java.security.krb5.conf", krb5Path);
        configuration.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(configuration);
        UserGroupInformation.loginUserFromKeytab(principal, keytabPath);
    }

    private ContainerLogsRequest buildContainerLogsRequest(
            YarnLogsDownloadRequest request, java.nio.file.Path temporaryDirectory) {
        ApplicationId appId = ApplicationId.fromString(request.getApplicationId());
        ContainerLogsRequest containerLogsRequest = new ContainerLogsRequest();
        containerLogsRequest.setAppId(appId);
        containerLogsRequest.setAppOwner(request.getAppOwner());
        containerLogsRequest.setBytes(Long.MAX_VALUE);
        containerLogsRequest.setOutputLocalDir(temporaryDirectory.toString());
        return containerLogsRequest;
    }

    private DownloadedYarnLogs buildDownloadResult(String applicationId, java.nio.file.Path temporaryDirectory)
            throws IOException {
        List<java.nio.file.Path> children = listChildren(temporaryDirectory);
        if (children.isEmpty()) {
            throw new YarnLogsDownloadException("YARN日志拉取成功但未生成任何日志文件");
        }
        if (children.size() == 1 && Files.isRegularFile(children.get(0))) {
            String fileName = FileNameCleaner.clean(children.get(0).getFileName().toString());
            return new DownloadedYarnLogs(temporaryDirectory, children.get(0), false, fileName);
        }
        return new DownloadedYarnLogs(
                temporaryDirectory,
                temporaryDirectory,
                true,
                FileNameCleaner.clean(applicationId) + ".zip");
    }

    private List<java.nio.file.Path> listChildren(java.nio.file.Path directory) throws IOException {
        List<java.nio.file.Path> children = new ArrayList<java.nio.file.Path>();
        java.nio.file.DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(directory);
        try {
            for (java.nio.file.Path child : stream) {
                children.add(child);
            }
        } finally {
            stream.close();
        }
        return children;
    }

    private void cleanupOnFailure(java.nio.file.Path temporaryDirectory, Throwable originalException) {
        if (temporaryDirectory != null) {
            try {
                TemporaryDirectoryCleaner.deleteRecursively(temporaryDirectory);
            } catch (YarnLogsDownloadException cleanupException) {
                originalException.addSuppressed(cleanupException);
            }
        }
    }
}
