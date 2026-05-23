/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

/**
 * YARN 资源利用率响应体，表达 NodeManager 或容器聚合的内存与 CPU 使用。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnResourceUtilizationSummary {

    private int virtualMemory;
    private int physicalMemory;
    private float cpu;

    public int getVirtualMemory() {
        return virtualMemory;
    }

    public void setVirtualMemory(int virtualMemory) {
        this.virtualMemory = virtualMemory;
    }

    public int getPhysicalMemory() {
        return physicalMemory;
    }

    public void setPhysicalMemory(int physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    public float getCpu() {
        return cpu;
    }

    public void setCpu(float cpu) {
        this.cpu = cpu;
    }
}
