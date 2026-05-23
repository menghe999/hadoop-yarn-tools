/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsRequest;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 基于 Hadoop YarnClient 的应用查询实现，负责 YarnClient 生命周期管理。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Component
public class DefaultYarnApplicationsClient implements YarnApplicationsClient {

    @Override
    public List<ApplicationReport> getApplications(
            Configuration configuration, Set<String> queues, EnumSet<YarnApplicationState> states, long limit)
            throws IOException, YarnException {
        return withClient(configuration, yarnClient -> {
            GetApplicationsRequest request = GetApplicationsRequest.newInstance();
            if (queues != null) {
                request.setQueues(queues);
            }
            request.setApplicationStates(states);
            request.setLimit(limit);
            return yarnClient.getApplications(request);
        });
    }

    @Override
    public ApplicationReport getApplicationReport(Configuration configuration, ApplicationId applicationId)
            throws IOException, YarnException {
        return withClient(configuration, yarnClient -> yarnClient.getApplicationReport(applicationId));
    }

    @Override
    public void killApplication(Configuration configuration, ApplicationId applicationId, String diagnostics)
            throws IOException, YarnException {
        withClient(configuration, yarnClient -> {
            yarnClient.killApplication(applicationId, diagnostics);
            return null;
        });
    }

    @Override
    public List<QueueInfo> getAllQueues(Configuration configuration) throws IOException, YarnException {
        return withClient(configuration, YarnClient::getAllQueues);
    }

    @Override
    public List<NodeReport> getNodeReports(Configuration configuration, NodeState... states)
            throws IOException, YarnException {
        return withClient(configuration, yarnClient -> yarnClient.getNodeReports(states));
    }

    private <T> T withClient(Configuration configuration, YarnClientCallback<T> callback)
            throws IOException, YarnException {
        YarnClient yarnClient = YarnClient.createYarnClient();
        try {
            yarnClient.init(configuration);
            yarnClient.start();
            return callback.execute(yarnClient);
        } finally {
            yarnClient.stop();
        }
    }

    /**
     * YarnClient 生命周期内执行的真实调用，统一透出 Hadoop 异常给 service 映射。
     *
     * @param <T> 调用返回类型
     */
    private interface YarnClientCallback<T> {

        /**
         * 执行 ResourceManager 客户端调用。
         *
         * @param yarnClient 已初始化并启动的 YarnClient
         * @return 调用结果
         * @throws IOException IO 异常
         * @throws YarnException YARN 客户端异常
         */
        T execute(YarnClient yarnClient) throws IOException, YarnException;
    }
}
