/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

/**
 * YARN 集群配置摘要响应体，仅暴露前端可安全展示的配置状态和脱敏认证信息。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnClusterSummary {

    private String clusterId;
    private boolean kerberosEnabled;
    private String authType;
    private boolean yarnConfigDirConfigured;
    private boolean principalConfigured;
    private String principalMasked;
    private boolean keytabPathConfigured;
    private boolean krb5PathConfigured;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public boolean isKerberosEnabled() {
        return kerberosEnabled;
    }

    public void setKerberosEnabled(boolean kerberosEnabled) {
        this.kerberosEnabled = kerberosEnabled;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public boolean isYarnConfigDirConfigured() {
        return yarnConfigDirConfigured;
    }

    public void setYarnConfigDirConfigured(boolean yarnConfigDirConfigured) {
        this.yarnConfigDirConfigured = yarnConfigDirConfigured;
    }

    public boolean isPrincipalConfigured() {
        return principalConfigured;
    }

    public void setPrincipalConfigured(boolean principalConfigured) {
        this.principalConfigured = principalConfigured;
    }

    public String getPrincipalMasked() {
        return principalMasked;
    }

    public void setPrincipalMasked(String principalMasked) {
        this.principalMasked = principalMasked;
    }

    public boolean isKeytabPathConfigured() {
        return keytabPathConfigured;
    }

    public void setKeytabPathConfigured(boolean keytabPathConfigured) {
        this.keytabPathConfigured = keytabPathConfigured;
    }

    public boolean isKrb5PathConfigured() {
        return krb5PathConfigured;
    }

    public void setKrb5PathConfigured(boolean krb5PathConfigured) {
        this.krb5PathConfigured = krb5PathConfigured;
    }
}
