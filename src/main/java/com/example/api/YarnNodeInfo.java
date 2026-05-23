/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import java.util.ArrayList;
import java.util.List;

/**
 * YARN NodeManager 节点响应体，表达 RM 视角下的节点状态、资源和健康信息。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnNodeInfo {

    private String nodeId;
    private String host;
    private int port;
    private String state;
    private String httpAddress;
    private String rackName;
    private int numContainers;
    private String healthReport;
    private long lastHealthReportTime;
    private YarnResourceSummary used;
    private YarnResourceSummary capability;
    private List<String> nodeLabels = new ArrayList<String>();
    private List<YarnNodeAttributeSummary> nodeAttributes = new ArrayList<YarnNodeAttributeSummary>();
    private YarnResourceUtilizationSummary nodeUtilization;
    private YarnResourceUtilizationSummary aggregatedContainersUtilization;
    private Integer decommissioningTimeout;
    private String nodeUpdateType;

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getHttpAddress() { return httpAddress; }
    public void setHttpAddress(String httpAddress) { this.httpAddress = httpAddress; }
    public String getRackName() { return rackName; }
    public void setRackName(String rackName) { this.rackName = rackName; }
    public int getNumContainers() { return numContainers; }
    public void setNumContainers(int numContainers) { this.numContainers = numContainers; }
    public String getHealthReport() { return healthReport; }
    public void setHealthReport(String healthReport) { this.healthReport = healthReport; }
    public long getLastHealthReportTime() { return lastHealthReportTime; }
    public void setLastHealthReportTime(long lastHealthReportTime) { this.lastHealthReportTime = lastHealthReportTime; }
    public YarnResourceSummary getUsed() { return used; }
    public void setUsed(YarnResourceSummary used) { this.used = used; }
    public YarnResourceSummary getCapability() { return capability; }
    public void setCapability(YarnResourceSummary capability) { this.capability = capability; }
    public List<String> getNodeLabels() { return nodeLabels; }
    public void setNodeLabels(List<String> nodeLabels) { this.nodeLabels = nodeLabels; }
    public List<YarnNodeAttributeSummary> getNodeAttributes() { return nodeAttributes; }
    public void setNodeAttributes(List<YarnNodeAttributeSummary> nodeAttributes) { this.nodeAttributes = nodeAttributes; }
    public YarnResourceUtilizationSummary getNodeUtilization() { return nodeUtilization; }
    public void setNodeUtilization(YarnResourceUtilizationSummary nodeUtilization) { this.nodeUtilization = nodeUtilization; }
    public YarnResourceUtilizationSummary getAggregatedContainersUtilization() { return aggregatedContainersUtilization; }
    public void setAggregatedContainersUtilization(YarnResourceUtilizationSummary aggregatedContainersUtilization) { this.aggregatedContainersUtilization = aggregatedContainersUtilization; }
    public Integer getDecommissioningTimeout() { return decommissioningTimeout; }
    public void setDecommissioningTimeout(Integer decommissioningTimeout) { this.decommissioningTimeout = decommissioningTimeout; }
    public String getNodeUpdateType() { return nodeUpdateType; }
    public void setNodeUpdateType(String nodeUpdateType) { this.nodeUpdateType = nodeUpdateType; }
}
