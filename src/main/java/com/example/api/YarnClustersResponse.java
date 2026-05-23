/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import java.util.ArrayList;
import java.util.List;

/**
 * YARN 集群配置列表响应体，包含应用查询上限和按集群标识排序后的安全摘要集合。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnClustersResponse {

    private long maxApplications;
    private List<YarnClusterSummary> clusters = new ArrayList<YarnClusterSummary>();

    public long getMaxApplications() {
        return maxApplications;
    }

    public void setMaxApplications(long maxApplications) {
        this.maxApplications = maxApplications;
    }

    public List<YarnClusterSummary> getClusters() {
        return clusters;
    }

    public void setClusters(List<YarnClusterSummary> clusters) {
        this.clusters = clusters;
    }
}
