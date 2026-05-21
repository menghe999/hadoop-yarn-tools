/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * YARN 集群连接服务，集中处理 Hadoop 配置加载和 Kerberos 全局认证边界。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Service
public class YarnClusterConnectionService {

    private static final Object AUTHENTICATION_LOCK = new Object();

    private static final String CORE_SITE = "core-site.xml";

    private static final String HDFS_SITE = "hdfs-site.xml";

    private static final String YARN_SITE = "yarn-site.xml";

    /**
     * 根据服务端集群配置加载 Hadoop 客户端配置文件。
     *
     * @param cluster 服务端集群配置
     * @return Hadoop 配置对象
     */
    public Configuration loadHadoopConfiguration(YarnClusterProperties.Cluster cluster) {
        Configuration configuration = new Configuration();
        String yarnConfigDir = cluster.getYarnConfigDir();
        configuration.addResource(new Path(yarnConfigDir + File.separator + CORE_SITE));
        configuration.addResource(new Path(yarnConfigDir + File.separator + HDFS_SITE));
        configuration.addResource(new Path(yarnConfigDir + File.separator + YARN_SITE));
        return configuration;
    }

    /**
     * 在集群认证上下文中执行 Hadoop API 调用；所有模式均串行化，防止 UGI 全局状态串用。
     *
     * @param cluster 服务端集群配置
     * @param configuration Hadoop 配置对象
     * @param operation 待执行操作
     * @return 操作结果
     * @throws Exception Hadoop 或业务执行异常
     */
    public <T> T execute(YarnClusterProperties.Cluster cluster, Configuration configuration,
            YarnClusterOperation<T> operation) throws Exception {
        synchronized (AUTHENTICATION_LOCK) {
            if (cluster.isKerberosEnabled()) {
                loginKerberos(configuration, cluster.getPrincipal(), cluster.getKeytabPath(), cluster.getKrb5Path());
            } else {
                loginSimple(configuration);
            }
            return operation.execute();
        }
    }

    private void loginSimple(Configuration configuration) throws IOException {
        UserGroupInformation.reset();
        configuration.set("hadoop.security.authentication", "simple");
        UserGroupInformation.setConfiguration(configuration);
        UserGroupInformation.loginUserFromSubject(null);
    }

    private void loginKerberos(Configuration configuration, String principal, String keytabPath, String krb5Path)
            throws IOException {
        UserGroupInformation.reset();
        System.setProperty("java.security.krb5.conf", krb5Path);
        configuration.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(configuration);
        UserGroupInformation.loginUserFromKeytab(principal, keytabPath);
    }
}
