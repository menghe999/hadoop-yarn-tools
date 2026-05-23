/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

/**
 * YARN 队列按分区配置的响应体，表达容量和有效资源上限下限。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnQueueConfiguration {

    private float capacity;
    private float absoluteCapacity;
    private float maxCapacity;
    private float absoluteMaxCapacity;
    private float maxAmPercentage;
    private YarnResourceSummary effectiveMinCapacity;
    private YarnResourceSummary effectiveMaxCapacity;
    private YarnResourceSummary configuredMinCapacity;
    private YarnResourceSummary configuredMaxCapacity;

    public float getCapacity() { return capacity; }
    public void setCapacity(float capacity) { this.capacity = capacity; }
    public float getAbsoluteCapacity() { return absoluteCapacity; }
    public void setAbsoluteCapacity(float absoluteCapacity) { this.absoluteCapacity = absoluteCapacity; }
    public float getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(float maxCapacity) { this.maxCapacity = maxCapacity; }
    public float getAbsoluteMaxCapacity() { return absoluteMaxCapacity; }
    public void setAbsoluteMaxCapacity(float absoluteMaxCapacity) { this.absoluteMaxCapacity = absoluteMaxCapacity; }
    public float getMaxAmPercentage() { return maxAmPercentage; }
    public void setMaxAmPercentage(float maxAmPercentage) { this.maxAmPercentage = maxAmPercentage; }
    public YarnResourceSummary getEffectiveMinCapacity() { return effectiveMinCapacity; }
    public void setEffectiveMinCapacity(YarnResourceSummary effectiveMinCapacity) { this.effectiveMinCapacity = effectiveMinCapacity; }
    public YarnResourceSummary getEffectiveMaxCapacity() { return effectiveMaxCapacity; }
    public void setEffectiveMaxCapacity(YarnResourceSummary effectiveMaxCapacity) { this.effectiveMaxCapacity = effectiveMaxCapacity; }
    public YarnResourceSummary getConfiguredMinCapacity() { return configuredMinCapacity; }
    public void setConfiguredMinCapacity(YarnResourceSummary configuredMinCapacity) { this.configuredMinCapacity = configuredMinCapacity; }
    public YarnResourceSummary getConfiguredMaxCapacity() { return configuredMaxCapacity; }
    public void setConfiguredMaxCapacity(YarnResourceSummary configuredMaxCapacity) { this.configuredMaxCapacity = configuredMaxCapacity; }
}
