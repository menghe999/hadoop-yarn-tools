/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.YarnApplicationsQueryService;
import com.example.service.YarnClusterQueryService;
import com.example.service.YarnLogsDownloadService;
import com.example.service.YarnRuntimeQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenAPI 文档测试，验证 Swagger 能发现 YARN 日志下载、应用查询和集群摘要接口。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YarnLogsDownloadService yarnLogsDownloadService;

    @MockBean
    private YarnApplicationsQueryService yarnApplicationsQueryService;

    @MockBean
    private YarnClusterQueryService yarnClusterQueryService;

    @MockBean
    private YarnRuntimeQueryService yarnRuntimeQueryService;

    /**
     * 验证 yarn 分组 OpenAPI JSON 包含日志、应用、队列、节点和集群接口路径。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldExposeYarnApisInOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs/yarn"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/yarn/logs/download")))
                .andExpect(content().string(containsString("/api/yarn/applications")))
                .andExpect(content().string(containsString("/api/yarn/applications/{applicationId}")))
                .andExpect(content().string(containsString("/api/yarn/applications/{applicationId}/kill")))
                .andExpect(content().string(containsString("/api/yarn/queues")))
                .andExpect(content().string(containsString("/api/yarn/nodes")))
                .andExpect(content().string(containsString("/api/yarn/nodes/{nodeId}")))
                .andExpect(content().string(containsString("/api/yarn/clusters")));
    }
}
