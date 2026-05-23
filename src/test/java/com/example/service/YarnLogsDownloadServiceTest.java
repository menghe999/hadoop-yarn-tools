/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnLogsDownloadRequest;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.logaggregation.ContainerLogsRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * YARN 日志下载服务单元测试，通过可注入 dumper 避免连接真实 Hadoop/YARN 集群。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnLogsDownloadServiceTest {

    /**
     * 验证服务层会用 YARN 应用报告中的提交用户构造 appOwner、bytes 和受控 outputLocalDir，并将单文件结果标记为直接下载。
     */
    @Test
    public void shouldBuildContainerLogsRequestAndReturnSingleFile() {
        RecordingDumper recordingDumper = new RecordingDumper(0, true);
        YarnLogsDownloadService service = new YarnLogsDownloadService(
                recordingDumper, clusterProperties(), new YarnClusterConnectionService(),
                new FakeYarnApplicationsClient("devops", null));

        DownloadedYarnLogs downloadedYarnLogs = service.download(validRequest());

        try {
            assertNotNull(recordingDumper.configuration);
            assertEquals("application_1732873473669_0058", recordingDumper.request.getAppId().toString());
            assertEquals("devops", recordingDumper.request.getAppOwner());
            assertEquals(Long.MAX_VALUE, recordingDumper.request.getBytes());
            assertEquals(downloadedYarnLogs.getTemporaryDirectory().toString(), recordingDumper.request.getOutputLocalDir());
            assertFalse(downloadedYarnLogs.isZipRequired());
            assertEquals("stdout.log", downloadedYarnLogs.getFileName());
        } finally {
            TemporaryDirectoryCleaner.deleteRecursively(downloadedYarnLogs.getTemporaryDirectory());
        }
    }

    /**
     * 验证 dumper 返回非 0 时抛出业务异常，并清理已经写入的服务端临时目录。
     */
    @Test
    public void shouldCleanTemporaryDirectoryWhenDumperReturnsFailure() {
        RecordingDumper recordingDumper = new RecordingDumper(1, true);
        YarnLogsDownloadService service = new YarnLogsDownloadService(
                recordingDumper, clusterProperties(), new YarnClusterConnectionService(),
                new FakeYarnApplicationsClient("devops", null));

        assertThrows(YarnLogsDownloadException.class, () -> service.download(validRequest()));

        assertNotNull(recordingDumper.outputLocalDir);
        assertFalse(Files.exists(recordingDumper.outputLocalDir));
    }

    /**
     * 验证 dumper 成功但没有生成日志文件时返回业务异常，避免空成功文件响应给前端。
     */
    @Test
    public void shouldRejectEmptyDownloadedLogs() {
        RecordingDumper recordingDumper = new RecordingDumper(0, false);
        YarnLogsDownloadService service = new YarnLogsDownloadService(
                recordingDumper, clusterProperties(), new YarnClusterConnectionService(),
                new FakeYarnApplicationsClient("devops", null));

        assertThrows(YarnLogsDownloadException.class, () -> service.download(validRequest()));
        assertFalse(Files.exists(recordingDumper.outputLocalDir));
    }

    /**
     * 验证 YARN 应用报告没有提交用户时返回 400 语义的请求异常，避免空 owner 进入日志聚合 API。
     */
    @Test
    public void shouldRejectBlankApplicationOwnerFromReport() {
        RecordingDumper recordingDumper = new RecordingDumper(0, true);
        YarnLogsDownloadService service = new YarnLogsDownloadService(
                recordingDumper, clusterProperties(), new YarnClusterConnectionService(),
                new FakeYarnApplicationsClient(" ", null));

        assertThrows(YarnLogsInvalidRequestException.class, () -> service.download(validRequest()));
    }

    /**
     * 验证查询 YARN 应用报告失败时包装为日志下载异常，不暴露为未知运行时异常。
     */
    @Test
    public void shouldWrapApplicationReportQueryFailure() {
        RecordingDumper recordingDumper = new RecordingDumper(0, true);
        YarnLogsDownloadService service = new YarnLogsDownloadService(
                recordingDumper, clusterProperties(), new YarnClusterConnectionService(),
                new FakeYarnApplicationsClient("devops", new YarnException("rm unavailable")));

        assertThrows(YarnLogsDownloadException.class, () -> service.download(validRequest()));
    }

    private YarnLogsDownloadRequest validRequest() {
        YarnLogsDownloadRequest request = new YarnLogsDownloadRequest();
        request.setApplicationId("application_1732873473669_0058");
        request.setClusterId("default");
        return request;
    }

    private YarnClusterProperties clusterProperties() {
        YarnClusterProperties properties = new YarnClusterProperties();
        YarnClusterProperties.Cluster cluster = new YarnClusterProperties.Cluster();
        cluster.setYarnConfigDir("/tmp/yarn-conf");
        properties.getClusters().put("default", cluster);
        return properties;
    }

    /**
     * 测试专用 dumper，记录服务层传入的 Hadoop 请求并按需写入虚拟日志文件。
     *
     * <p>
     * created on: 2026-05-20
     * </p>
     *
     * @author mengh
     * @since 1.1.0
     */
    private static class RecordingDumper implements YarnLogsDumper {

        private final int result;

        private final boolean writeLogFile;

        private Configuration configuration;

        private ContainerLogsRequest request;

        private Path outputLocalDir;

        private RecordingDumper(int result, boolean writeLogFile) {
            this.result = result;
            this.writeLogFile = writeLogFile;
        }

        /**
         * 记录日志拉取请求，并在 outputLocalDir 中写入模拟日志文件以替代真实 YARN 集群。
         *
         * @param configuration Hadoop 客户端配置
         * @param request 容器日志请求
         * @return 预设返回码
         * @throws IOException 写入模拟日志文件失败
         */
        @Override
        public int dump(Configuration configuration, ContainerLogsRequest request) throws IOException {
            this.configuration = configuration;
            this.request = request;
            this.outputLocalDir = java.nio.file.Paths.get(request.getOutputLocalDir());
            if (writeLogFile) {
                Files.write(outputLocalDir.resolve("stdout.log"), "stdout".getBytes(StandardCharsets.UTF_8));
            }
            return result;
        }
    }

    /**
     * 测试专用 YARN 应用查询客户端，只模拟应用报告 owner 查询，不连接真实 ResourceManager。
     *
     * <p>
     * created on: 2026-05-22
     * </p>
     *
     * @author mengh
     * @since 1.1.0
     */
    private static class FakeYarnApplicationsClient implements YarnApplicationsClient {

        private final String appOwner;

        private final Exception failure;

        private FakeYarnApplicationsClient(String appOwner, Exception failure) {
            this.appOwner = appOwner;
            this.failure = failure;
        }

        /**
         * 返回带有预设提交用户的应用报告，或按测试场景抛出查询异常。
         *
         * @param configuration Hadoop 客户端配置
         * @param applicationId YARN 应用 ID
         * @return 模拟应用报告
         * @throws IOException 模拟 IO 查询异常
         * @throws YarnException 模拟 YARN 查询异常
         */
        @Override
        public ApplicationReport getApplicationReport(Configuration configuration, ApplicationId applicationId)
                throws IOException, YarnException {
            if (failure instanceof IOException) {
                throw (IOException) failure;
            }
            if (failure instanceof YarnException) {
                throw (YarnException) failure;
            }
            ApplicationReport applicationReport = mock(ApplicationReport.class);
            when(applicationReport.getUser()).thenReturn(appOwner);
            return applicationReport;
        }

        @Override
        public List<ApplicationReport> getApplications(
                Configuration configuration, Set<String> queues, EnumSet<YarnApplicationState> states, long limit) {
            throw new UnsupportedOperationException("测试不需要查询应用列表");
        }

        @Override
        public void killApplication(Configuration configuration, ApplicationId applicationId, String diagnostics) {
            throw new UnsupportedOperationException("测试不需要终止应用");
        }

        @Override
        public List<QueueInfo> getAllQueues(Configuration configuration) {
            throw new UnsupportedOperationException("测试不需要查询队列");
        }

        @Override
        public List<NodeReport> getNodeReports(Configuration configuration, NodeState... states) {
            throw new UnsupportedOperationException("测试不需要查询节点");
        }
    }
}
