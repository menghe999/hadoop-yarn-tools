/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

/**
 * YARN 应用终止响应体，返回终止请求提交前的应用状态和操作结果说明。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationKillResponse {

    private String clusterId;
    private String applicationId;
    private String previousState;
    private boolean killRequested;
    private String message;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public boolean isKillRequested() {
        return killRequested;
    }

    public void setKillRequested(boolean killRequested) {
        this.killRequested = killRequested;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
