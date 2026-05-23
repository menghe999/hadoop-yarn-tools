/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import com.example.api.YarnClusterSummary;
import com.example.api.YarnClustersResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * YARN 集群配置查询服务，将服务端白名单配置映射为不含敏感路径和原始凭据的只读摘要。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Service
public class YarnClusterQueryService {

    private static final String AUTH_TYPE_KERBEROS = "KERBEROS";
    private static final String AUTH_TYPE_SIMPLE = "SIMPLE";

    private final YarnClusterProperties yarnClusterProperties;

    public YarnClusterQueryService(YarnClusterProperties yarnClusterProperties) {
        this.yarnClusterProperties = yarnClusterProperties;
    }

    /**
     * 查询已配置的 YARN 集群安全摘要，不校验配置完整性，也不连接 Hadoop 或 YARN。
     *
     * @return 集群配置摘要响应
     */
    public YarnClustersResponse listClusters() {
        YarnClustersResponse response = new YarnClustersResponse();
        response.setMaxApplications(yarnClusterProperties.getMaxApplications());

        List<YarnClusterSummary> summaries = new ArrayList<YarnClusterSummary>();
        Map<String, YarnClusterProperties.Cluster> clusters = yarnClusterProperties.getClusters();
        if (clusters != null) {
            List<String> clusterIds = new ArrayList<String>(clusters.keySet());
            Collections.sort(clusterIds);
            for (String clusterId : clusterIds) {
                summaries.add(toSummary(clusterId, clusters.get(clusterId)));
            }
        }
        response.setClusters(summaries);
        return response;
    }

    private YarnClusterSummary toSummary(String clusterId, YarnClusterProperties.Cluster cluster) {
        YarnClusterSummary summary = new YarnClusterSummary();
        summary.setClusterId(clusterId);
        if (cluster == null) {
            summary.setAuthType(AUTH_TYPE_SIMPLE);
            return summary;
        }
        summary.setKerberosEnabled(cluster.isKerberosEnabled());
        summary.setAuthType(cluster.isKerberosEnabled() ? AUTH_TYPE_KERBEROS : AUTH_TYPE_SIMPLE);
        summary.setYarnConfigDirConfigured(isConfigured(cluster.getYarnConfigDir()));
        summary.setPrincipalConfigured(isConfigured(cluster.getPrincipal()));
        summary.setPrincipalMasked(maskPrincipal(cluster.getPrincipal()));
        summary.setKeytabPathConfigured(isConfigured(cluster.getKeytabPath()));
        summary.setKrb5PathConfigured(isConfigured(cluster.getKrb5Path()));
        return summary;
    }

    private String maskPrincipal(String principal) {
        if (!isConfigured(principal)) {
            return null;
        }
        String trimmedPrincipal = principal.trim();
        int realmSeparator = trimmedPrincipal.indexOf('@');
        if (realmSeparator < 0) {
            return "***";
        }
        return trimmedPrincipal.substring(0, realmSeparator + 1) + "***";
    }

    private boolean isConfigured(String value) {
        return value != null && value.trim().length() > 0;
    }
}
