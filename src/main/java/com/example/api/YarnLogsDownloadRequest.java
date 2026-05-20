/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import javax.validation.constraints.AssertTrue;
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
    private String applicationId;

    @NotBlank(message = "appOwner不能为空")
    @Pattern(regexp = "[A-Za-z0-9._-]{1,128}", message = "appOwner格式不正确")
    private String appOwner;

    @NotBlank(message = "clusterId不能为空")
    @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确")
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

    /**
     * 禁止 HTTP 请求直接传入服务端本地路径或 Kerberos 凭据字段，敏感路径必须由服务端配置白名单维护。
     *
     * @return 请求体是否没有携带敏感路径字段
     */
    @AssertTrue(message = "请求中不能传入yarnConfigDir、principal、keytabPath或krb5Path")
    public boolean isSensitivePathArgumentsAbsent() {
        return unknownFields.isEmpty();
    }
}
