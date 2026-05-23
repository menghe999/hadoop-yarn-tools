/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnApplicationsQueryRequest;
import com.example.api.YarnApplicationsResponse;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;
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
        assertEquals(Collections.singleton("root.default"), applicationsClient.requests.get(0).queues);
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
     * 验证 state 为空时会显式传递所有状态，避免 ResourceManager 默认只返回 active 应用。
     */
    @Test
    public void shouldQueryApplicationsWithAllStatesWhenStateFilterIsBlank() {
        RecordingApplicationsClient applicationsClient = new RecordingApplicationsClient(false);
        YarnApplicationsQueryService service = service(applicationsClient);

        YarnApplicationsResponse response = service.query(request(null));

        assertEquals(2, applicationsClient.requests.size());
        assertEquals(Collections.singleton("root.default"), applicationsClient.requests.get(0).queues);
        assertEquals(
                EnumSet.of(YarnApplicationState.NEW, YarnApplicationState.NEW_SAVING, YarnApplicationState.SUBMITTED,
                        YarnApplicationState.ACCEPTED, YarnApplicationState.RUNNING),
                applicationsClient.requests.get(0).states);
        assertNull(applicationsClient.requests.get(1).queues);
        assertEquals(
                EnumSet.of(YarnApplicationState.FINISHED, YarnApplicationState.FAILED, YarnApplicationState.KILLED),
                applicationsClient.requests.get(1).states);
        assertNull(response.getState());
        assertEquals(2, response.getApplications().size());
    }

    /**
     * 验证查询 FINISHED 时不下推队列过滤，改为服务端按队列过滤，兼容部分 RM 对终态队列过滤返回空的问题。
     */
    @Test
    public void shouldFilterFinishedApplicationsByQueueLocally() {
        RecordingApplicationsClient applicationsClient = new RecordingApplicationsClient(false);
        YarnApplicationsQueryService service = service(applicationsClient);

        YarnApplicationsResponse response = service.query(request("FINISHED"));

        assertEquals(1, applicationsClient.requests.size());
        assertNull(applicationsClient.requests.get(0).queues);
        assertEquals(EnumSet.of(YarnApplicationState.FINISHED), applicationsClient.requests.get(0).states);
        assertEquals(1, response.getApplications().size());
        assertEquals("application_1776741130114_0007", response.getApplications().get(0).getApplicationId());
        assertEquals("FINISHED", response.getApplications().get(0).getState());
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

    private static ApplicationReport completedReport(String queue) {
        return ApplicationReport.newInstance(
                ApplicationId.newInstance(1776741130114L, 7),
                null,
                "root",
                queue,
                "finished-job",
                "rm-host",
                8088,
                null,
                YarnApplicationState.FINISHED,
                "",
                "http://rm/cluster/app/application_1776741130114_0007",
                1776741130114L,
                1776741130999L,
                1776741140999L,
                FinalApplicationStatus.SUCCEEDED,
                null,
                "MAPREDUCE",
                1.0f,
                "http://rm/cluster/app/application_1776741130114_0007",
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
        private EnumSet<YarnApplicationState> states;
        private List<RecordedRequest> requests = new ArrayList<RecordedRequest>();

        private RecordingApplicationsClient(boolean fail) {
            this.fail = fail;
        }

        @Override
        public List<ApplicationReport> getApplications(
                Configuration configuration, Set<String> queues, EnumSet<YarnApplicationState> states, long limit)
                throws IOException, YarnException {
            this.configuration = configuration;
            this.states = states;
            this.limit = limit;
            this.requests.add(new RecordedRequest(queues, states));
            if (fail) {
                throw new YarnException("ResourceManager unavailable");
            }
            if (states.contains(YarnApplicationState.FINISHED)) {
                List<ApplicationReport> reports = new ArrayList<ApplicationReport>();
                reports.add(completedReport("root.default"));
                reports.add(completedReport("root.bigdata"));
                return reports;
            }
            return Collections.singletonList(report());
        }

        @Override
        public ApplicationReport getApplicationReport(Configuration configuration, ApplicationId applicationId)
                throws IOException, YarnException {
            return report();
        }

        @Override
        public void killApplication(Configuration configuration, ApplicationId applicationId, String diagnostics)
                throws IOException, YarnException {
        }

        @Override
        public List<QueueInfo> getAllQueues(Configuration configuration) throws IOException, YarnException {
            return Collections.emptyList();
        }

        @Override
        public List<NodeReport> getNodeReports(Configuration configuration, NodeState... states)
                throws IOException, YarnException {
            return Collections.emptyList();
        }

        private long limit;
    }

    private static class RecordedRequest {

        private final Set<String> queues;
        private final EnumSet<YarnApplicationState> states;

        private RecordedRequest(Set<String> queues, EnumSet<YarnApplicationState> states) {
            this.queues = queues;
            this.states = states;
        }
    }
}
