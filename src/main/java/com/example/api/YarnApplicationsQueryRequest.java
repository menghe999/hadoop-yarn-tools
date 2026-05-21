/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * YARN 应用列表查询请求参数，支持按集群、队列和可选状态过滤。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationsQueryRequest {

    @NotBlank(message = "clusterId不能为空")
    @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确")
    @Schema(description = "服务端白名单集群标识", example = "default")
    private String clusterId;

    @NotBlank(message = "queue不能为空")
    @Pattern(regexp = "[A-Za-z0-9._-]{1,128}", message = "queue格式不正确")
    @Schema(description = "YARN队列名称", example = "root.default")
    private String queue;

    @Pattern(regexp = "|[A-Za-z_]{1,32}", message = "state格式不正确")
    @Schema(description = "YARN应用状态；为空时查询所有状态", example = "RUNNING", allowableValues = {
            "NEW", "NEW_SAVING", "SUBMITTED", "ACCEPTED", "RUNNING", "FINISHED", "FAILED", "KILLED"})
    private String state;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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
}
