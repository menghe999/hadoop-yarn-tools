/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

/**
 * YARN 应用资源用量响应体，承载 RM 返回的容器、资源和累计资源秒指标。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationResourceUsage {

    private int numUsedContainers;
    private int numReservedContainers;
    private YarnResourceSummary used;
    private YarnResourceSummary reserved;
    private YarnResourceSummary needed;
    private long memorySeconds;
    private long vcoreSeconds;
    private float queueUsagePercentage;
    private float clusterUsagePercentage;

    public int getNumUsedContainers() {
        return numUsedContainers;
    }

    public void setNumUsedContainers(int numUsedContainers) {
        this.numUsedContainers = numUsedContainers;
    }

    public int getNumReservedContainers() {
        return numReservedContainers;
    }

    public void setNumReservedContainers(int numReservedContainers) {
        this.numReservedContainers = numReservedContainers;
    }

    public YarnResourceSummary getUsed() {
        return used;
    }

    public void setUsed(YarnResourceSummary used) {
        this.used = used;
    }

    public YarnResourceSummary getReserved() {
        return reserved;
    }

    public void setReserved(YarnResourceSummary reserved) {
        this.reserved = reserved;
    }

    public YarnResourceSummary getNeeded() {
        return needed;
    }

    public void setNeeded(YarnResourceSummary needed) {
        this.needed = needed;
    }

    public long getMemorySeconds() {
        return memorySeconds;
    }

    public void setMemorySeconds(long memorySeconds) {
        this.memorySeconds = memorySeconds;
    }

    public long getVcoreSeconds() {
        return vcoreSeconds;
    }

    public void setVcoreSeconds(long vcoreSeconds) {
        this.vcoreSeconds = vcoreSeconds;
    }

    public float getQueueUsagePercentage() {
        return queueUsagePercentage;
    }

    public void setQueueUsagePercentage(float queueUsagePercentage) {
        this.queueUsagePercentage = queueUsagePercentage;
    }

    public float getClusterUsagePercentage() {
        return clusterUsagePercentage;
    }

    public void setClusterUsagePercentage(float clusterUsagePercentage) {
        this.clusterUsagePercentage = clusterUsagePercentage;
    }
}
