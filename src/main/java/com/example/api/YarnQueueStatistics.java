/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

/**
 * YARN 队列统计响应体，表达应用数量、用户数量和资源分配统计。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnQueueStatistics {

    private long numAppsSubmitted;
    private long numAppsRunning;
    private long numAppsPending;
    private long numAppsCompleted;
    private long numAppsKilled;
    private long numAppsFailed;
    private long numActiveUsers;
    private long availableMemoryMb;
    private long allocatedMemoryMb;
    private long pendingMemoryMb;
    private long reservedMemoryMb;
    private long availableVCores;
    private long allocatedVCores;
    private long pendingVCores;
    private long reservedVCores;
    private long pendingContainers;
    private long allocatedContainers;
    private long reservedContainers;

    public long getNumAppsSubmitted() { return numAppsSubmitted; }
    public void setNumAppsSubmitted(long numAppsSubmitted) { this.numAppsSubmitted = numAppsSubmitted; }
    public long getNumAppsRunning() { return numAppsRunning; }
    public void setNumAppsRunning(long numAppsRunning) { this.numAppsRunning = numAppsRunning; }
    public long getNumAppsPending() { return numAppsPending; }
    public void setNumAppsPending(long numAppsPending) { this.numAppsPending = numAppsPending; }
    public long getNumAppsCompleted() { return numAppsCompleted; }
    public void setNumAppsCompleted(long numAppsCompleted) { this.numAppsCompleted = numAppsCompleted; }
    public long getNumAppsKilled() { return numAppsKilled; }
    public void setNumAppsKilled(long numAppsKilled) { this.numAppsKilled = numAppsKilled; }
    public long getNumAppsFailed() { return numAppsFailed; }
    public void setNumAppsFailed(long numAppsFailed) { this.numAppsFailed = numAppsFailed; }
    public long getNumActiveUsers() { return numActiveUsers; }
    public void setNumActiveUsers(long numActiveUsers) { this.numActiveUsers = numActiveUsers; }
    public long getAvailableMemoryMb() { return availableMemoryMb; }
    public void setAvailableMemoryMb(long availableMemoryMb) { this.availableMemoryMb = availableMemoryMb; }
    public long getAllocatedMemoryMb() { return allocatedMemoryMb; }
    public void setAllocatedMemoryMb(long allocatedMemoryMb) { this.allocatedMemoryMb = allocatedMemoryMb; }
    public long getPendingMemoryMb() { return pendingMemoryMb; }
    public void setPendingMemoryMb(long pendingMemoryMb) { this.pendingMemoryMb = pendingMemoryMb; }
    public long getReservedMemoryMb() { return reservedMemoryMb; }
    public void setReservedMemoryMb(long reservedMemoryMb) { this.reservedMemoryMb = reservedMemoryMb; }
    public long getAvailableVCores() { return availableVCores; }
    public void setAvailableVCores(long availableVCores) { this.availableVCores = availableVCores; }
    public long getAllocatedVCores() { return allocatedVCores; }
    public void setAllocatedVCores(long allocatedVCores) { this.allocatedVCores = allocatedVCores; }
    public long getPendingVCores() { return pendingVCores; }
    public void setPendingVCores(long pendingVCores) { this.pendingVCores = pendingVCores; }
    public long getReservedVCores() { return reservedVCores; }
    public void setReservedVCores(long reservedVCores) { this.reservedVCores = reservedVCores; }
    public long getPendingContainers() { return pendingContainers; }
    public void setPendingContainers(long pendingContainers) { this.pendingContainers = pendingContainers; }
    public long getAllocatedContainers() { return allocatedContainers; }
    public void setAllocatedContainers(long allocatedContainers) { this.allocatedContainers = allocatedContainers; }
    public long getReservedContainers() { return reservedContainers; }
    public void setReservedContainers(long reservedContainers) { this.reservedContainers = reservedContainers; }
}
