/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsRequest;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
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
        YarnClient yarnClient = YarnClient.createYarnClient();
        try {
            yarnClient.init(configuration);
            yarnClient.start();
            GetApplicationsRequest request = GetApplicationsRequest.newInstance();
            if (queues != null) {
                request.setQueues(queues);
            }
            request.setApplicationStates(states);
            request.setLimit(limit);
            return yarnClient.getApplications(request);
        } finally {
            yarnClient.stop();
        }
    }
}
