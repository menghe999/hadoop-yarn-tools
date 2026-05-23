/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YARN 队列运行时信息响应体，递归表达容量、抢占、节点标签和子队列。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnQueueInfo {

    private String queueName;
    private String name;
    private String state;
    private float capacity;
    private float currentCapacity;
    private float maximumCapacity;
    private List<String> accessibleNodeLabels = new ArrayList<String>();
    private String defaultNodeLabelExpression;
    private Boolean preemptionDisabled;
    private Boolean intraQueuePreemptionDisabled;
    private YarnQueueStatistics statistics;
    private Map<String, YarnQueueConfiguration> queueConfigurations = new LinkedHashMap<String, YarnQueueConfiguration>();
    private List<YarnQueueInfo> childQueues = new ArrayList<YarnQueueInfo>();

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public float getCapacity() { return capacity; }
    public void setCapacity(float capacity) { this.capacity = capacity; }
    public float getCurrentCapacity() { return currentCapacity; }
    public void setCurrentCapacity(float currentCapacity) { this.currentCapacity = currentCapacity; }
    public float getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(float maximumCapacity) { this.maximumCapacity = maximumCapacity; }
    public List<String> getAccessibleNodeLabels() { return accessibleNodeLabels; }
    public void setAccessibleNodeLabels(List<String> accessibleNodeLabels) { this.accessibleNodeLabels = accessibleNodeLabels; }
    public String getDefaultNodeLabelExpression() { return defaultNodeLabelExpression; }
    public void setDefaultNodeLabelExpression(String defaultNodeLabelExpression) { this.defaultNodeLabelExpression = defaultNodeLabelExpression; }
    public Boolean getPreemptionDisabled() { return preemptionDisabled; }
    public void setPreemptionDisabled(Boolean preemptionDisabled) { this.preemptionDisabled = preemptionDisabled; }
    public Boolean getIntraQueuePreemptionDisabled() { return intraQueuePreemptionDisabled; }
    public void setIntraQueuePreemptionDisabled(Boolean intraQueuePreemptionDisabled) { this.intraQueuePreemptionDisabled = intraQueuePreemptionDisabled; }
    public YarnQueueStatistics getStatistics() { return statistics; }
    public void setStatistics(YarnQueueStatistics statistics) { this.statistics = statistics; }
    public Map<String, YarnQueueConfiguration> getQueueConfigurations() { return queueConfigurations; }
    public void setQueueConfigurations(Map<String, YarnQueueConfiguration> queueConfigurations) { this.queueConfigurations = queueConfigurations; }
    public List<YarnQueueInfo> getChildQueues() { return childQueues; }
    public void setChildQueues(List<YarnQueueInfo> childQueues) { this.childQueues = childQueues; }
}
