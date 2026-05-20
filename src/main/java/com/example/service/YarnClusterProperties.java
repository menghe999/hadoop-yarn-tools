/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * YARN 集群服务端白名单配置，避免请求直接传入本地配置文件或 Kerberos 凭据路径。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@ConfigurationProperties(prefix = "yarn.logs")
public class YarnClusterProperties {

    private Map<String, Cluster> clusters = new HashMap<String, Cluster>();

    public Map<String, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, Cluster> clusters) {
        this.clusters = clusters;
    }

    /**
     * 按集群标识获取服务端预配置的集群连接信息。
     *
     * @param clusterId 请求中的集群标识
     * @return 集群配置
     */
    public Cluster requireCluster(String clusterId) {
        Cluster cluster = clusters.get(clusterId);
        if (cluster == null) {
            throw new YarnLogsInvalidRequestException("未知YARN集群标识: " + clusterId);
        }
        cluster.validate(clusterId);
        return cluster;
    }

    /**
     * 单个 YARN 集群的 Hadoop 配置目录和 Kerberos 凭据路径。
     *
     * <p>
     * created on: 2026-05-20
     * </p>
     *
     * @author mengh
     * @since 1.1.0
     */
    public static class Cluster {

        private String yarnConfigDir;

        private boolean kerberosEnabled;

        private String principal;

        private String keytabPath;

        private String krb5Path;

        public String getYarnConfigDir() {
            return yarnConfigDir;
        }

        public void setYarnConfigDir(String yarnConfigDir) {
            this.yarnConfigDir = yarnConfigDir;
        }

        public boolean isKerberosEnabled() {
            return kerberosEnabled;
        }

        public void setKerberosEnabled(boolean kerberosEnabled) {
            this.kerberosEnabled = kerberosEnabled;
        }

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public String getKeytabPath() {
            return keytabPath;
        }

        public void setKeytabPath(String keytabPath) {
            this.keytabPath = keytabPath;
        }

        public String getKrb5Path() {
            return krb5Path;
        }

        public void setKrb5Path(String krb5Path) {
            this.krb5Path = krb5Path;
        }

        private void validate(String clusterId) {
            if (isBlank(yarnConfigDir)) {
                throw new YarnLogsDownloadException("YARN集群配置缺少yarnConfigDir: " + clusterId);
            }
            if (kerberosEnabled && (isBlank(principal) || isBlank(keytabPath) || isBlank(krb5Path))) {
                throw new YarnLogsDownloadException("YARN集群Kerberos配置不完整: " + clusterId);
            }
        }

        private boolean isBlank(String value) {
            return value == null || value.trim().length() == 0;
        }
    }
}
