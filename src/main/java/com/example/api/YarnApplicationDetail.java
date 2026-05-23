/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import java.util.ArrayList;
import java.util.List;

/**
 * YARN 应用详情响应体，面向前端展示应用状态、跟踪地址、时间线和资源用量。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationDetail {

    private String clusterId;
    private String applicationId;
    private String attemptId;
    private String name;
    private String user;
    private String queue;
    private String state;
    private String finalStatus;
    private String applicationType;
    private float progress;
    private String trackingUrl;
    private String originalTrackingUrl;
    private String diagnostics;
    private String host;
    private int rpcPort;
    private long submitTime;
    private long startedTime;
    private long launchTime;
    private long finishedTime;
    private long elapsedTime;
    private List<String> applicationTags = new ArrayList<String>();
    private String logAggregationStatus;
    private YarnApplicationResourceUsage resourceUsage;

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

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getOriginalTrackingUrl() {
        return originalTrackingUrl;
    }

    public void setOriginalTrackingUrl(String originalTrackingUrl) {
        this.originalTrackingUrl = originalTrackingUrl;
    }

    public String getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(String diagnostics) {
        this.diagnostics = diagnostics;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }

    public long getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(long startedTime) {
        this.startedTime = startedTime;
    }

    public long getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(long launchTime) {
        this.launchTime = launchTime;
    }

    public long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public List<String> getApplicationTags() {
        return applicationTags;
    }

    public void setApplicationTags(List<String> applicationTags) {
        this.applicationTags = applicationTags;
    }

    public String getLogAggregationStatus() {
        return logAggregationStatus;
    }

    public void setLogAggregationStatus(String logAggregationStatus) {
        this.logAggregationStatus = logAggregationStatus;
    }

    public YarnApplicationResourceUsage getResourceUsage() {
        return resourceUsage;
    }

    public void setResourceUsage(YarnApplicationResourceUsage resourceUsage) {
        this.resourceUsage = resourceUsage;
    }
}
