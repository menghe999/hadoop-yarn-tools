/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnApplicationDetail;
import com.example.api.YarnApplicationKillResponse;
import com.example.api.YarnApplicationResourceUsage;
import com.example.api.YarnNodeAttributeSummary;
import com.example.api.YarnNodeInfo;
import com.example.api.YarnNodesResponse;
import com.example.api.YarnQueueConfiguration;
import com.example.api.YarnQueueInfo;
import com.example.api.YarnQueueStatistics;
import com.example.api.YarnQueuesResponse;
import com.example.api.YarnResourceSummary;
import com.example.api.YarnResourceUtilizationSummary;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationResourceUsageReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LogAggregationStatus;
import org.apache.hadoop.yarn.api.records.NodeAttribute;
import org.apache.hadoop.yarn.api.records.NodeAttributeKey;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.QueueConfigurations;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.QueueState;
import org.apache.hadoop.yarn.api.records.QueueStatistics;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceUtilization;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * YARN 运行时查询服务，复用集群白名单和认证边界查询应用详情、队列、节点并提交 kill 请求。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Service
public class YarnRuntimeQueryService {

    private static final String KILL_DIAGNOSTICS = "Killed by hadoop-yarn-tools API";

    private final YarnApplicationsClient yarnApplicationsClient;
    private final YarnClusterProperties yarnClusterProperties;
    private final YarnClusterConnectionService yarnClusterConnectionService;

    public YarnRuntimeQueryService(YarnApplicationsClient yarnApplicationsClient,
            YarnClusterProperties yarnClusterProperties, YarnClusterConnectionService yarnClusterConnectionService) {
        this.yarnApplicationsClient = yarnApplicationsClient;
        this.yarnClusterProperties = yarnClusterProperties;
        this.yarnClusterConnectionService = yarnClusterConnectionService;
    }

    public YarnApplicationDetail getApplicationDetail(String clusterId, String applicationId) {
        try {
            YarnClusterProperties.Cluster cluster = yarnClusterProperties.requireCluster(clusterId);
            final Configuration configuration = yarnClusterConnectionService.loadHadoopConfiguration(cluster);
            final ApplicationId parsedApplicationId = ApplicationId.fromString(applicationId);
            ApplicationReport report = yarnClusterConnectionService.execute(
                    cluster, configuration, () -> yarnApplicationsClient.getApplicationReport(configuration, parsedApplicationId));
            return toApplicationDetail(clusterId, report);
        } catch (YarnLogsInvalidRequestException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new YarnLogsInvalidRequestException("applicationId参数不支持");
        } catch (Exception e) {
            throw new YarnApplicationsQueryException("YARN应用详情查询失败", e);
        }
    }

    public YarnApplicationKillResponse killApplication(String clusterId, String applicationId) {
        try {
            if (!yarnClusterProperties.isApplicationKillEnabled()) {
                throw new YarnLogsInvalidRequestException("YARN应用kill接口未启用");
            }
            YarnClusterProperties.Cluster cluster = yarnClusterProperties.requireCluster(clusterId);
            final Configuration configuration = yarnClusterConnectionService.loadHadoopConfiguration(cluster);
            final ApplicationId parsedApplicationId = ApplicationId.fromString(applicationId);
            ApplicationReport report = yarnClusterConnectionService.execute(
                    cluster, configuration, () -> yarnApplicationsClient.getApplicationReport(configuration, parsedApplicationId));
            YarnApplicationState previousState = report.getYarnApplicationState();
            if (isTerminal(previousState)) {
                throw new YarnLogsInvalidRequestException("终态应用不能执行kill: " + previousState.name());
            }
            yarnClusterConnectionService.execute(cluster, configuration, () -> {
                yarnApplicationsClient.killApplication(configuration, parsedApplicationId, KILL_DIAGNOSTICS);
                return null;
            });
            YarnApplicationKillResponse response = new YarnApplicationKillResponse();
            response.setClusterId(clusterId);
            response.setApplicationId(applicationId);
            response.setPreviousState(enumName(previousState));
            response.setKillRequested(true);
            response.setMessage("YARN应用kill请求已提交");
            return response;
        } catch (YarnLogsInvalidRequestException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new YarnLogsInvalidRequestException("applicationId参数不支持");
        } catch (Exception e) {
            throw new YarnApplicationsQueryException("YARN应用kill失败", e);
        }
    }

    public YarnQueuesResponse listQueues(String clusterId) {
        try {
            YarnClusterProperties.Cluster cluster = yarnClusterProperties.requireCluster(clusterId);
            final Configuration configuration = yarnClusterConnectionService.loadHadoopConfiguration(cluster);
            List<QueueInfo> queues = yarnClusterConnectionService.execute(
                    cluster, configuration, () -> yarnApplicationsClient.getAllQueues(configuration));
            YarnQueuesResponse response = new YarnQueuesResponse();
            response.setClusterId(clusterId);
            List<YarnQueueInfo> mappedQueues = new ArrayList<YarnQueueInfo>();
            for (QueueInfo queue : safeList(queues)) {
                mappedQueues.add(toQueueInfo(queue));
            }
            response.setQueues(mappedQueues);
            return response;
        } catch (YarnLogsInvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new YarnApplicationsQueryException("YARN队列查询失败", e);
        }
    }

    public YarnNodesResponse listNodes(String clusterId, String state) {
        try {
            YarnClusterProperties.Cluster cluster = yarnClusterProperties.requireCluster(clusterId);
            final Configuration configuration = yarnClusterConnectionService.loadHadoopConfiguration(cluster);
            final NodeState[] states = parseNodeStates(state);
            List<NodeReport> reports = yarnClusterConnectionService.execute(
                    cluster, configuration, () -> yarnApplicationsClient.getNodeReports(configuration, states));
            YarnNodesResponse response = new YarnNodesResponse();
            response.setClusterId(clusterId);
            response.setState(state);
            List<YarnNodeInfo> nodes = new ArrayList<YarnNodeInfo>();
            for (NodeReport report : safeList(reports)) {
                nodes.add(toNodeInfo(report));
            }
            response.setNodes(nodes);
            return response;
        } catch (YarnLogsInvalidRequestException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new YarnLogsInvalidRequestException("state参数不支持");
        } catch (Exception e) {
            throw new YarnApplicationsQueryException("YARN节点查询失败", e);
        }
    }

    public YarnNodeInfo getNodeDetail(String clusterId, String nodeId) {
        YarnNodesResponse response = listNodes(clusterId, null);
        for (YarnNodeInfo node : response.getNodes()) {
            if (nodeId.equals(node.getNodeId())) {
                return node;
            }
        }
        throw new YarnLogsInvalidRequestException("YARN节点不存在: " + nodeId);
    }

    private boolean isTerminal(YarnApplicationState state) {
        return state == YarnApplicationState.FINISHED || state == YarnApplicationState.FAILED
                || state == YarnApplicationState.KILLED;
    }

    private YarnApplicationDetail toApplicationDetail(String clusterId, ApplicationReport report) {
        YarnApplicationDetail detail = new YarnApplicationDetail();
        detail.setClusterId(clusterId);
        detail.setApplicationId(stringValue(report.getApplicationId()));
        detail.setAttemptId(stringValue(report.getCurrentApplicationAttemptId()));
        detail.setName(report.getName());
        detail.setUser(report.getUser());
        detail.setQueue(report.getQueue());
        detail.setState(enumName(report.getYarnApplicationState()));
        detail.setFinalStatus(enumName(report.getFinalApplicationStatus()));
        detail.setApplicationType(report.getApplicationType());
        detail.setProgress(report.getProgress());
        detail.setTrackingUrl(report.getTrackingUrl());
        detail.setOriginalTrackingUrl(report.getOriginalTrackingUrl());
        detail.setDiagnostics(report.getDiagnostics());
        detail.setHost(report.getHost());
        detail.setRpcPort(report.getRpcPort());
        detail.setSubmitTime(report.getSubmitTime());
        detail.setStartedTime(report.getStartTime());
        detail.setLaunchTime(report.getLaunchTime());
        detail.setFinishedTime(report.getFinishTime());
        detail.setElapsedTime(elapsedTime(report.getStartTime(), report.getFinishTime()));
        detail.setApplicationTags(sortedStrings(report.getApplicationTags()));
        detail.setLogAggregationStatus(enumName(report.getLogAggregationStatus()));
        detail.setResourceUsage(toApplicationResourceUsage(report.getApplicationResourceUsageReport()));
        return detail;
    }

    private YarnApplicationResourceUsage toApplicationResourceUsage(ApplicationResourceUsageReport usageReport) {
        if (usageReport == null) {
            return null;
        }
        YarnApplicationResourceUsage usage = new YarnApplicationResourceUsage();
        usage.setNumUsedContainers(usageReport.getNumUsedContainers());
        usage.setNumReservedContainers(usageReport.getNumReservedContainers());
        usage.setUsed(toResourceSummary(usageReport.getUsedResources()));
        usage.setReserved(toResourceSummary(usageReport.getReservedResources()));
        usage.setNeeded(toResourceSummary(usageReport.getNeededResources()));
        usage.setMemorySeconds(usageReport.getMemorySeconds());
        usage.setVcoreSeconds(usageReport.getVcoreSeconds());
        usage.setQueueUsagePercentage(usageReport.getQueueUsagePercentage());
        usage.setClusterUsagePercentage(usageReport.getClusterUsagePercentage());
        return usage;
    }

    private YarnQueueInfo toQueueInfo(QueueInfo queue) {
        YarnQueueInfo dto = new YarnQueueInfo();
        dto.setQueueName(queue.getQueueName());
        dto.setName(queue.getQueueName());
        dto.setState(enumName(queue.getQueueState()));
        dto.setCapacity(queue.getCapacity());
        dto.setCurrentCapacity(queue.getCurrentCapacity());
        dto.setMaximumCapacity(queue.getMaximumCapacity());
        dto.setAccessibleNodeLabels(sortedStrings(queue.getAccessibleNodeLabels()));
        dto.setDefaultNodeLabelExpression(queue.getDefaultNodeLabelExpression());
        dto.setPreemptionDisabled(queue.getPreemptionDisabled());
        dto.setIntraQueuePreemptionDisabled(queue.getIntraQueuePreemptionDisabled());
        dto.setStatistics(toQueueStatistics(queue.getQueueStatistics()));
        dto.setQueueConfigurations(toQueueConfigurations(queue.getQueueConfigurations()));
        List<YarnQueueInfo> childQueues = new ArrayList<YarnQueueInfo>();
        for (QueueInfo childQueue : safeList(queue.getChildQueues())) {
            childQueues.add(toQueueInfo(childQueue));
        }
        dto.setChildQueues(childQueues);
        return dto;
    }

    private YarnQueueStatistics toQueueStatistics(QueueStatistics statistics) {
        if (statistics == null) {
            return null;
        }
        YarnQueueStatistics dto = new YarnQueueStatistics();
        dto.setNumAppsSubmitted(statistics.getNumAppsSubmitted());
        dto.setNumAppsRunning(statistics.getNumAppsRunning());
        dto.setNumAppsPending(statistics.getNumAppsPending());
        dto.setNumAppsCompleted(statistics.getNumAppsCompleted());
        dto.setNumAppsKilled(statistics.getNumAppsKilled());
        dto.setNumAppsFailed(statistics.getNumAppsFailed());
        dto.setNumActiveUsers(statistics.getNumActiveUsers());
        dto.setAvailableMemoryMb(statistics.getAvailableMemoryMB());
        dto.setAllocatedMemoryMb(statistics.getAllocatedMemoryMB());
        dto.setPendingMemoryMb(statistics.getPendingMemoryMB());
        dto.setReservedMemoryMb(statistics.getReservedMemoryMB());
        dto.setAvailableVCores(statistics.getAvailableVCores());
        dto.setAllocatedVCores(statistics.getAllocatedVCores());
        dto.setPendingVCores(statistics.getPendingVCores());
        dto.setReservedVCores(statistics.getReservedVCores());
        dto.setPendingContainers(statistics.getPendingContainers());
        dto.setAllocatedContainers(statistics.getAllocatedContainers());
        dto.setReservedContainers(statistics.getReservedContainers());
        return dto;
    }

    private Map<String, YarnQueueConfiguration> toQueueConfigurations(
            Map<String, QueueConfigurations> queueConfigurations) {
        Map<String, YarnQueueConfiguration> mapped = new LinkedHashMap<String, YarnQueueConfiguration>();
        if (queueConfigurations == null) {
            return mapped;
        }
        List<String> labels = new ArrayList<String>(queueConfigurations.keySet());
        Collections.sort(labels);
        for (String label : labels) {
            mapped.put(label, toQueueConfiguration(queueConfigurations.get(label)));
        }
        return mapped;
    }

    private YarnQueueConfiguration toQueueConfiguration(QueueConfigurations configuration) {
        if (configuration == null) {
            return null;
        }
        YarnQueueConfiguration dto = new YarnQueueConfiguration();
        dto.setCapacity(configuration.getCapacity());
        dto.setAbsoluteCapacity(configuration.getAbsoluteCapacity());
        dto.setMaxCapacity(configuration.getMaxCapacity());
        dto.setAbsoluteMaxCapacity(configuration.getAbsoluteMaxCapacity());
        dto.setMaxAmPercentage(configuration.getMaxAMPercentage());
        dto.setEffectiveMinCapacity(toResourceSummary(configuration.getEffectiveMinCapacity()));
        dto.setEffectiveMaxCapacity(toResourceSummary(configuration.getEffectiveMaxCapacity()));
        dto.setConfiguredMinCapacity(toResourceSummary(configuration.getConfiguredMinCapacity()));
        dto.setConfiguredMaxCapacity(toResourceSummary(configuration.getConfiguredMaxCapacity()));
        return dto;
    }

    private YarnNodeInfo toNodeInfo(NodeReport report) {
        YarnNodeInfo dto = new YarnNodeInfo();
        NodeId nodeId = report.getNodeId();
        dto.setNodeId(stringValue(nodeId));
        if (nodeId != null) {
            dto.setHost(nodeId.getHost());
            dto.setPort(nodeId.getPort());
        }
        dto.setState(enumName(report.getNodeState()));
        dto.setHttpAddress(report.getHttpAddress());
        dto.setRackName(report.getRackName());
        dto.setNumContainers(report.getNumContainers());
        dto.setHealthReport(report.getHealthReport());
        dto.setLastHealthReportTime(report.getLastHealthReportTime());
        dto.setUsed(toResourceSummary(report.getUsed()));
        dto.setCapability(toResourceSummary(report.getCapability()));
        dto.setNodeLabels(sortedStrings(report.getNodeLabels()));
        dto.setNodeAttributes(toNodeAttributes(report.getNodeAttributes()));
        dto.setNodeUtilization(toResourceUtilization(report.getNodeUtilization()));
        dto.setAggregatedContainersUtilization(toResourceUtilization(report.getAggregatedContainersUtilization()));
        dto.setDecommissioningTimeout(report.getDecommissioningTimeout());
        dto.setNodeUpdateType(enumName(report.getNodeUpdateType()));
        return dto;
    }

    private List<YarnNodeAttributeSummary> toNodeAttributes(Set<NodeAttribute> attributes) {
        List<YarnNodeAttributeSummary> mapped = new ArrayList<YarnNodeAttributeSummary>();
        if (attributes == null) {
            return mapped;
        }
        List<NodeAttribute> sorted = new ArrayList<NodeAttribute>(attributes);
        Collections.sort(sorted, (left, right) -> stringValue(left).compareTo(stringValue(right)));
        for (NodeAttribute attribute : sorted) {
            YarnNodeAttributeSummary dto = new YarnNodeAttributeSummary();
            NodeAttributeKey key = attribute.getAttributeKey();
            if (key != null) {
                dto.setPrefix(key.getAttributePrefix());
                dto.setName(key.getAttributeName());
            }
            dto.setType(enumName(attribute.getAttributeType()));
            dto.setValue(attribute.getAttributeValue());
            mapped.add(dto);
        }
        return mapped;
    }

    private YarnResourceSummary toResourceSummary(Resource resource) {
        if (resource == null) {
            return null;
        }
        YarnResourceSummary summary = new YarnResourceSummary();
        summary.setMemoryMb(resource.getMemorySize());
        summary.setVirtualCores(resource.getVirtualCores());
        return summary;
    }

    private YarnResourceUtilizationSummary toResourceUtilization(ResourceUtilization utilization) {
        if (utilization == null) {
            return null;
        }
        YarnResourceUtilizationSummary summary = new YarnResourceUtilizationSummary();
        summary.setVirtualMemory(utilization.getVirtualMemory());
        summary.setPhysicalMemory(utilization.getPhysicalMemory());
        summary.setCpu(utilization.getCPU());
        return summary;
    }

    private NodeState[] parseNodeStates(String state) {
        if (state == null || state.trim().length() == 0) {
            return new NodeState[0];
        }
        return new NodeState[] {NodeState.valueOf(state.trim().toUpperCase(Locale.ENGLISH))};
    }

    private long elapsedTime(long startedTime, long finishedTime) {
        if (startedTime <= 0L) {
            return 0L;
        }
        long endTime = finishedTime > 0L ? finishedTime : System.currentTimeMillis();
        return Math.max(0L, endTime - startedTime);
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private String enumName(QueueState value) {
        return value == null ? null : value.name();
    }

    private String enumName(FinalApplicationStatus value) {
        return value == null ? null : value.name();
    }

    private String enumName(LogAggregationStatus value) {
        return value == null ? null : value.name();
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private List<String> sortedStrings(Set<String> values) {
        if (values == null) {
            return new ArrayList<String>();
        }
        List<String> sorted = new ArrayList<String>(values);
        Collections.sort(sorted);
        return sorted;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? new ArrayList<T>() : values;
    }
}
