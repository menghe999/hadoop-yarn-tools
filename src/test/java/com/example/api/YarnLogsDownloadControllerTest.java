/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.DownloadedYarnLogs;
import com.example.service.YarnLogsDownloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * YARN 日志下载控制器单元测试，验证响应头、参数校验、ZIP 流式响应和临时目录清理。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@WebMvcTest(YarnLogsDownloadController.class)
public class YarnLogsDownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YarnLogsDownloadService yarnLogsDownloadService;

    /**
     * 验证单文件下载成功时返回 attachment 响应头、原始文件内容，并在响应结束后清理临时目录。
     *
     * @throws Exception MockMvc 或文件系统异常
     */
    @Test
    public void shouldReturnSingleFileDownloadAndCleanTemporaryDirectory() throws Exception {
        Path temporaryDirectory = Files.createTempDirectory("controller-single-");
        Path logFile = temporaryDirectory.resolve("application.log");
        Files.write(logFile, "log-content".getBytes(StandardCharsets.UTF_8));
        when(yarnLogsDownloadService.download(any(YarnLogsDownloadRequest.class)))
                .thenReturn(new DownloadedYarnLogs(temporaryDirectory, logFile, false, "application.log"));

        MvcResult mvcResult = mockMvc.perform(post("/api/yarn/logs/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"application.log\""));

        assertEquals("log-content", mvcResult.getResponse().getContentAsString());
        assertFalse(Files.exists(temporaryDirectory));
    }

    /**
     * 验证请求缺少必要字段时返回 400，避免空参数进入 YARN 日志拉取流程。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/yarn/logs/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 验证请求直接传入本地路径或 Kerberos 凭据路径时返回 400，防止前端控制服务端敏感文件路径。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldRejectSensitivePathArgumentsFromRequest() throws Exception {
        mockMvc.perform(post("/api/yarn/logs/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"applicationId\":\"application_1732873473669_0058\","
                                + "\"clusterId\":\"default\","
                                + "\"yarnConfigDir\":\"/tmp/yarn-conf\""
                                + "}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 验证旧版 appOwner 入参会作为未知字段返回 400，防止前端继续控制日志 appOwner。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldRejectAppOwnerArgumentFromRequest() throws Exception {
        mockMvc.perform(post("/api/yarn/logs/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"applicationId\":\"application_1732873473669_0058\","
                                + "\"clusterId\":\"default\","
                                + "\"appOwner\":\"devops\""
                                + "}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 验证 user 入参会作为未知字段返回 400，防止前端用别名绕过 owner 解析。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldRejectUserArgumentFromRequest() throws Exception {
        mockMvc.perform(post("/api/yarn/logs/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"applicationId\":\"application_1732873473669_0058\","
                                + "\"clusterId\":\"default\","
                                + "\"user\":\"devops\""
                                + "}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 验证多文件或目录下载时返回 ZIP，并清洗 ZIP entry 名称防止 Zip Slip。
     *
     * @throws Exception MockMvc、文件系统或 ZIP 解析异常
     */
    @Test
    public void shouldReturnZipDownloadAndCleanTemporaryDirectory() throws Exception {
        Path temporaryDirectory = Files.createTempDirectory("controller-zip-");
        Path nestedDirectory = temporaryDirectory.resolve("container/../container_01").normalize();
        Files.createDirectories(nestedDirectory);
        Files.write(nestedDirectory.resolve("stdout.log"), "stdout".getBytes(StandardCharsets.UTF_8));
        Files.write(temporaryDirectory.resolve("stderr.log"), "stderr".getBytes(StandardCharsets.UTF_8));
        when(yarnLogsDownloadService.download(any(YarnLogsDownloadRequest.class)))
                .thenReturn(new DownloadedYarnLogs(temporaryDirectory, temporaryDirectory, true, "application.zip"));

        MvcResult mvcResult = mockMvc.perform(post("/api/yarn/logs/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"application.zip\""));

        List<String> entryNames = zipEntryNames(mvcResult.getResponse().getContentAsByteArray());
        assertTrue(entryNames.contains("container_01/stdout.log"));
        assertTrue(entryNames.contains("stderr.log"));
        for (String entryName : entryNames) {
            assertFalse(entryName.contains(".."));
            assertFalse(entryName.startsWith("/"));
        }
        assertFalse(Files.exists(temporaryDirectory));
    }

    private String validRequestJson() {
        return "{"
                + "\"applicationId\":\"application_1732873473669_0058\","
                + "\"clusterId\":\"default\""
                + "}";
    }

    private List<String> zipEntryNames(byte[] content) throws Exception {
        List<String> entryNames = new ArrayList<String>();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(content));
        try {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryNames.add(entry.getName());
                zipInputStream.closeEntry();
            }
        } finally {
            zipInputStream.close();
        }
        return entryNames;
    }
}
