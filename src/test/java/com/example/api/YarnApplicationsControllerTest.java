/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.YarnApplicationsQueryException;
import com.example.service.YarnApplicationsQueryService;
import com.example.service.YarnLogsInvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * YARN 应用列表控制器单元测试，验证查询参数绑定、JSON 响应和错误映射。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@WebMvcTest(YarnApplicationsController.class)
public class YarnApplicationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YarnApplicationsQueryService yarnApplicationsQueryService;

    /**
     * 验证带 state 的队列应用查询返回查询条件和应用摘要。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldReturnApplicationsByQueueAndState() throws Exception {
        when(yarnApplicationsQueryService.query(any(YarnApplicationsQueryRequest.class)))
                .thenReturn(response("RUNNING"));

        mockMvc.perform(get("/api/yarn/applications")
                        .param("clusterId", "default")
                        .param("queue", "root.default")
                        .param("state", "RUNNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clusterId").value("default"))
                .andExpect(jsonPath("$.queue").value("root.default"))
                .andExpect(jsonPath("$.state").value("RUNNING"))
                .andExpect(jsonPath("$.applications", hasSize(1)))
                .andExpect(jsonPath("$.applications[0].applicationId").value("application_1776741130114_0006"))
                .andExpect(jsonPath("$.applications[0].state").value("RUNNING"));
    }

    /**
     * 验证不传 state 时仍可查询队列下所有应用状态。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldReturnApplicationsWithoutStateFilter() throws Exception {
        when(yarnApplicationsQueryService.query(any(YarnApplicationsQueryRequest.class)))
                .thenReturn(response(null));

        mockMvc.perform(get("/api/yarn/applications")
                        .param("clusterId", "default")
                        .param("queue", "root.default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").doesNotExist())
                .andExpect(jsonPath("$.applications", hasSize(1)));
    }

    /**
     * 验证缺少队列参数时返回 400，避免全平台无边界查询。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldRejectMissingQueue() throws Exception {
        mockMvc.perform(get("/api/yarn/applications")
                        .param("clusterId", "default"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 验证业务层识别非法 state 后映射为 400。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldMapInvalidStateToBadRequest() throws Exception {
        when(yarnApplicationsQueryService.query(any(YarnApplicationsQueryRequest.class)))
                .thenThrow(new YarnLogsInvalidRequestException("state参数不支持"));

        mockMvc.perform(get("/api/yarn/applications")
                        .param("clusterId", "default")
                        .param("queue", "root.default")
                        .param("state", "BROKEN"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 验证 YARN 查询失败映射为 502。
     *
     * @throws Exception MockMvc 异常
     */
    @Test
    public void shouldMapYarnFailureToBadGateway() throws Exception {
        when(yarnApplicationsQueryService.query(any(YarnApplicationsQueryRequest.class)))
                .thenThrow(new YarnApplicationsQueryException("YARN应用列表查询失败", new RuntimeException("rm down")));

        mockMvc.perform(get("/api/yarn/applications")
                        .param("clusterId", "default")
                        .param("queue", "root.default"))
                .andExpect(status().isBadGateway());
    }

    private YarnApplicationsResponse response(String state) {
        YarnApplicationsResponse response = new YarnApplicationsResponse();
        response.setClusterId("default");
        response.setQueue("root.default");
        response.setState(state);
        YarnApplicationSummary summary = new YarnApplicationSummary();
        summary.setApplicationId("application_1776741130114_0006");
        summary.setName("demo-job");
        summary.setUser("root");
        summary.setQueue("root.default");
        summary.setState("RUNNING");
        summary.setFinalStatus("UNDEFINED");
        summary.setApplicationType("MAPREDUCE");
        summary.setProgress(0.5f);
        summary.setTrackingUrl("http://rm/proxy/application_1776741130114_0006/");
        summary.setStartedTime(1776741130114L);
        summary.setFinishedTime(0L);
        response.setApplications(Collections.singletonList(summary));
        return response;
    }
}
