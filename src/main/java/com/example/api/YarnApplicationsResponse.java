/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import java.util.ArrayList;
import java.util.List;

/**
 * YARN 应用列表查询响应体，保留本次查询条件和匹配的应用摘要集合。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationsResponse {

    private String clusterId;
    private String queue;
    private String state;
    private List<YarnApplicationSummary> applications = new ArrayList<YarnApplicationSummary>();

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<YarnApplicationSummary> getApplications() {
        return applications;
    }

    public void setApplications(List<YarnApplicationSummary> applications) {
        this.applications = applications;
    }
}
