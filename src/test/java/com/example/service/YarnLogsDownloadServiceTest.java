/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnLogsDownloadRequest;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.logaggregation.ContainerLogsRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
     * 验证服务层会构造 appId、appOwner、bytes 和受控 outputLocalDir，并将单文件结果标记为直接下载。
     */
    @Test
    public void shouldBuildContainerLogsRequestAndReturnSingleFile() {
        RecordingDumper recordingDumper = new RecordingDumper(0, true);
        YarnLogsDownloadService service = new YarnLogsDownloadService(recordingDumper, clusterProperties());

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
        YarnLogsDownloadService service = new YarnLogsDownloadService(recordingDumper, clusterProperties());

        assertThrows(YarnLogsDownloadException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                service.download(validRequest());
            }
        });

        assertNotNull(recordingDumper.outputLocalDir);
        assertFalse(Files.exists(recordingDumper.outputLocalDir));
    }

    /**
     * 验证 dumper 成功但没有生成日志文件时返回业务异常，避免空成功文件响应给前端。
     */
    @Test
    public void shouldRejectEmptyDownloadedLogs() {
        RecordingDumper recordingDumper = new RecordingDumper(0, false);
        YarnLogsDownloadService service = new YarnLogsDownloadService(recordingDumper, clusterProperties());

        assertThrows(YarnLogsDownloadException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                service.download(validRequest());
            }
        });
        assertFalse(Files.exists(recordingDumper.outputLocalDir));
    }

    private YarnLogsDownloadRequest validRequest() {
        YarnLogsDownloadRequest request = new YarnLogsDownloadRequest();
        request.setApplicationId("application_1732873473669_0058");
        request.setAppOwner("devops");
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
}
