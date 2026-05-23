/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * YARN 应用终止请求体，只允许前端传入服务端白名单集群标识。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationKillRequest {

    @NotBlank(message = "clusterId不能为空")
    @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确")
    @Schema(description = "服务端白名单集群标识", example = "default")
    private String clusterId;

    @JsonIgnore
    private final Map<String, Object> unknownFields = new HashMap<String, Object>();

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

    @AssertTrue(message = "kill请求不能包含未知字段或服务端敏感路径")
    @JsonIgnore
    public boolean isUnknownFieldsEmpty() {
        return unknownFields.isEmpty();
    }
}
