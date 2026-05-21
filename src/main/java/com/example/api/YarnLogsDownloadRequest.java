/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * YARN 聚合日志下载请求体，承载应用标识、提交用户、Hadoop 配置目录和 Kerberos 凭据路径。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnLogsDownloadRequest {

    @NotBlank(message = "applicationId不能为空")
    @Pattern(regexp = "application_\\d+_\\d+", message = "applicationId格式不正确")
    @Schema(description = "YARN应用ID", example = "application_1732873473669_0058")
    private String applicationId;

    @NotBlank(message = "appOwner不能为空")
    @Pattern(regexp = "[A-Za-z0-9._-]{1,128}", message = "appOwner格式不正确")
    @Schema(description = "应用提交用户", example = "devops")
    private String appOwner;

    @NotBlank(message = "clusterId不能为空")
    @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确")
    @Schema(description = "服务端白名单集群标识", example = "default")
    private String clusterId;

    private final Map<String, Object> unknownFields = new HashMap<String, Object>();

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }
}
