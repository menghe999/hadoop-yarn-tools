/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnApplicationSummary;
import com.example.api.YarnApplicationsQueryRequest;
import com.example.api.YarnApplicationsResponse;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * YARN 应用列表查询服务，按服务端集群配置、队列和可选状态读取 ResourceManager 应用报告。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Service
public class YarnApplicationsQueryService {

    private final YarnApplicationsClient yarnApplicationsClient;
    private final YarnClusterProperties yarnClusterProperties;
    private final YarnClusterConnectionService yarnClusterConnectionService;

    public YarnApplicationsQueryService(YarnApplicationsClient yarnApplicationsClient,
            YarnClusterProperties yarnClusterProperties, YarnClusterConnectionService yarnClusterConnectionService) {
        this.yarnApplicationsClient = yarnApplicationsClient;
        this.yarnClusterProperties = yarnClusterProperties;
        this.yarnClusterConnectionService = yarnClusterConnectionService;
    }

    /**
     * 查询指定队列下的 YARN 应用列表，并按可选状态过滤。
     *
     * @param request 查询请求参数
     * @return 应用列表响应
     */
    public YarnApplicationsResponse query(YarnApplicationsQueryRequest request) {
        try {
            YarnClusterProperties.Cluster cluster = yarnClusterProperties.requireCluster(request.getClusterId());
            final Configuration configuration = yarnClusterConnectionService.loadHadoopConfiguration(cluster);
            final EnumSet<YarnApplicationState> states = parseState(request.getState());
            final long limit = yarnClusterProperties.getMaxApplications();
            List<ApplicationReport> reports = yarnClusterConnectionService.execute(
                    cluster,
                    configuration,
                    () -> queryApplications(configuration, request.getQueue(), states, limit));
            return buildResponse(request, reports);
        } catch (YarnLogsInvalidRequestException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new YarnLogsInvalidRequestException("state参数不支持");
        } catch (Exception e) {
            throw new YarnApplicationsQueryException("YARN应用列表查询失败", e);
        }
    }

    private List<ApplicationReport> queryApplications(
            Configuration configuration, String queue, EnumSet<YarnApplicationState> states, long limit) throws Exception {
        Map<String, ApplicationReport> applications = new LinkedHashMap<String, ApplicationReport>();
        EnumSet<YarnApplicationState> activeStates = copyIntersection(states, activeStates());
        if (!activeStates.isEmpty()) {
            addApplications(applications, yarnApplicationsClient.getApplications(
                    configuration, Collections.singleton(queue), activeStates, limit));
        }
        EnumSet<YarnApplicationState> completedStates = copyIntersection(states, completedStates());
        if (!completedStates.isEmpty() && applications.size() < limit) {
            List<ApplicationReport> completedApplications = yarnApplicationsClient.getApplications(
                    configuration, null, completedStates, limit);
            for (ApplicationReport report : completedApplications) {
                if (isSameQueue(queue, report.getQueue())) {
                    applications.put(report.getApplicationId().toString(), report);
                    if (applications.size() >= limit) {
                        break;
                    }
                }
            }
        }
        return new ArrayList<ApplicationReport>(applications.values());
    }

    private EnumSet<YarnApplicationState> activeStates() {
        return EnumSet.of(
                YarnApplicationState.NEW,
                YarnApplicationState.NEW_SAVING,
                YarnApplicationState.SUBMITTED,
                YarnApplicationState.ACCEPTED,
                YarnApplicationState.RUNNING);
    }

    private EnumSet<YarnApplicationState> completedStates() {
        return EnumSet.of(YarnApplicationState.FINISHED, YarnApplicationState.FAILED, YarnApplicationState.KILLED);
    }

    private EnumSet<YarnApplicationState> copyIntersection(
            EnumSet<YarnApplicationState> source, EnumSet<YarnApplicationState> candidates) {
        EnumSet<YarnApplicationState> copy = EnumSet.copyOf(source);
        copy.retainAll(candidates);
        return copy;
    }

    private void addApplications(Map<String, ApplicationReport> applications, List<ApplicationReport> reports) {
        for (ApplicationReport report : reports) {
            applications.put(report.getApplicationId().toString(), report);
        }
    }

    private boolean isSameQueue(String requestedQueue, String reportQueue) {
        if (requestedQueue.equals(reportQueue)) {
            return true;
        }
        return reportQueue != null && reportQueue.endsWith("." + requestedQueue);
    }

    private EnumSet<YarnApplicationState> parseState(String state) {
        if (state == null || state.trim().length() == 0) {
            return EnumSet.allOf(YarnApplicationState.class);
        }
        YarnApplicationState yarnApplicationState = YarnApplicationState.valueOf(
                state.trim().toUpperCase(Locale.ENGLISH));
        return EnumSet.of(yarnApplicationState);
    }

    private YarnApplicationsResponse buildResponse(YarnApplicationsQueryRequest request, List<ApplicationReport> reports) {
        YarnApplicationsResponse response = new YarnApplicationsResponse();
        response.setClusterId(request.getClusterId());
        response.setQueue(request.getQueue());
        response.setState(request.getState());
        List<YarnApplicationSummary> applications = new ArrayList<YarnApplicationSummary>();
        for (ApplicationReport report : reports) {
            applications.add(toSummary(report));
        }
        response.setApplications(applications);
        return response;
    }

    private YarnApplicationSummary toSummary(ApplicationReport report) {
        YarnApplicationSummary summary = new YarnApplicationSummary();
        summary.setApplicationId(report.getApplicationId().toString());
        summary.setName(report.getName());
        summary.setUser(report.getUser());
        summary.setQueue(report.getQueue());
        summary.setState(report.getYarnApplicationState().name());
        summary.setFinalStatus(report.getFinalApplicationStatus().name());
        summary.setApplicationType(report.getApplicationType());
        summary.setProgress(report.getProgress());
        summary.setTrackingUrl(report.getTrackingUrl());
        summary.setStartedTime(report.getStartTime());
        summary.setFinishedTime(report.getFinishTime());
        return summary;
    }
}
