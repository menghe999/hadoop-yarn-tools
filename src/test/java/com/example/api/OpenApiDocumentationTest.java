/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.YarnApplicationsQueryService;
import com.example.service.YarnLogsDownloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenAPI 文档测试，验证 Swagger 能发现 YARN 日志下载和应用查询接口。
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
public class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YarnLogsDownloadService yarnLogsDownloadService;

    @MockBean
    private YarnApplicationsQueryService yarnApplicationsQueryService;

    /**
     * 验证 yarn 分组 OpenAPI JSON 包含两个对外接口路径，Swagger UI 依赖该文档渲染调试表单。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldExposeYarnApisInOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs/yarn"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/yarn/logs/download")))
                .andExpect(content().string(containsString("/api/yarn/applications")));
    }
}
