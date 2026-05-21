/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnApplicationsQueryRequest;
import com.example.api.YarnApplicationsResponse;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * YARN 应用列表查询服务单元测试，通过可注入客户端避免连接真实 ResourceManager。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationsQueryServiceTest {

    /**
     * 验证服务层会把队列和 state 过滤条件传递给 YarnClient 边界，并映射应用摘要字段。
     */
    @Test
    public void shouldQueryApplicationsByQueueAndState() {
        RecordingApplicationsClient applicationsClient = new RecordingApplicationsClient(false);
        YarnApplicationsQueryService service = service(applicationsClient);

        YarnApplicationsResponse response = service.query(request("RUNNING"));

        assertNotNull(applicationsClient.configuration);
        assertEquals(Collections.singleton("root.default"), applicationsClient.queues);
        assertEquals(EnumSet.of(YarnApplicationState.RUNNING), applicationsClient.states);
        assertEquals(1000L, applicationsClient.limit);
        assertEquals("default", response.getClusterId());
        assertEquals("root.default", response.getQueue());
        assertEquals("RUNNING", response.getState());
        assertEquals(1, response.getApplications().size());
        assertEquals("application_1776741130114_0006", response.getApplications().get(0).getApplicationId());
        assertEquals("demo-job", response.getApplications().get(0).getName());
    }

    /**
     * 验证 state 为空时不会向 YarnClient 传递状态过滤集合。
     */
    @Test
    public void shouldQueryApplicationsWithoutStateFilter() {
        RecordingApplicationsClient applicationsClient = new RecordingApplicationsClient(false);
        YarnApplicationsQueryService service = service(applicationsClient);

        YarnApplicationsResponse response = service.query(request(null));

        assertNull(applicationsClient.states);
        assertNull(response.getState());
        assertEquals(1, response.getApplications().size());
    }

    /**
     * 验证非法 state 会作为客户端参数错误返回。
     */
    @Test
    public void shouldRejectUnsupportedState() {
        YarnApplicationsQueryService service = service(new RecordingApplicationsClient(false));

        assertThrows(YarnLogsInvalidRequestException.class, () -> service.query(request("BROKEN")));
    }

    /**
     * 验证 ResourceManager 查询异常会映射为应用查询业务异常。
     */
    @Test
    public void shouldWrapYarnClientFailure() {
        YarnApplicationsQueryService service = service(new RecordingApplicationsClient(true));

        assertThrows(YarnApplicationsQueryException.class, () -> service.query(request("RUNNING")));
    }

    private YarnApplicationsQueryService service(YarnApplicationsClient applicationsClient) {
        return new YarnApplicationsQueryService(
                applicationsClient, clusterProperties(), new YarnClusterConnectionService());
    }

    private YarnApplicationsQueryRequest request(String state) {
        YarnApplicationsQueryRequest request = new YarnApplicationsQueryRequest();
        request.setClusterId("default");
        request.setQueue("root.default");
        request.setState(state);
        return request;
    }

    private YarnClusterProperties clusterProperties() {
        YarnClusterProperties properties = new YarnClusterProperties();
        YarnClusterProperties.Cluster cluster = new YarnClusterProperties.Cluster();
        cluster.setYarnConfigDir("/tmp/yarn-conf");
        properties.getClusters().put("default", cluster);
        return properties;
    }

    private static ApplicationReport report() {
        return ApplicationReport.newInstance(
                ApplicationId.newInstance(1776741130114L, 6),
                null,
                "root",
                "root.default",
                "demo-job",
                "rm-host",
                8088,
                null,
                YarnApplicationState.RUNNING,
                "",
                "http://rm/proxy/application_1776741130114_0006/",
                1776741130114L,
                1776741130999L,
                0L,
                FinalApplicationStatus.UNDEFINED,
                null,
                "MAPREDUCE",
                0.5f,
                "http://rm/proxy/application_1776741130114_0006/",
                null);
    }

    /**
     * 测试专用 YARN 应用客户端，记录查询条件并返回模拟应用报告。
     *
     * <p>
     * created on: 2026-05-20
     * </p>
     *
     * @author mengh
     * @since 1.1.0
     */
    private static class RecordingApplicationsClient implements YarnApplicationsClient {

        private final boolean fail;
        private Configuration configuration;
        private Set<String> queues;
        private EnumSet<YarnApplicationState> states;

        private RecordingApplicationsClient(boolean fail) {
            this.fail = fail;
        }

        @Override
        public List<ApplicationReport> getApplications(
                Configuration configuration, Set<String> queues, EnumSet<YarnApplicationState> states, long limit)
                throws IOException, YarnException {
            this.configuration = configuration;
            this.queues = queues;
            this.states = states;
            this.limit = limit;
            if (fail) {
                throw new YarnException("ResourceManager unavailable");
            }
            return Collections.singletonList(report());
        }

        private long limit;
    }
}
