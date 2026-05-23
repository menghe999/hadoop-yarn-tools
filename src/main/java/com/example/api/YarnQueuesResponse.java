/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import java.util.ArrayList;
import java.util.List;

/**
 * YARN 队列查询响应体，保留集群标识和 ResourceManager 返回的运行时队列树。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnQueuesResponse {

    private String clusterId;
    private List<YarnQueueInfo> queues = new ArrayList<YarnQueueInfo>();

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public List<YarnQueueInfo> getQueues() {
        return queues;
    }

    public void setQueues(List<YarnQueueInfo> queues) {
        this.queues = queues;
    }
}
