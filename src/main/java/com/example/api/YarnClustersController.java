/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.YarnClusterQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * YARN 集群配置控制器，提供服务端白名单集群的只读安全摘要查询接口。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@RestController
@RequestMapping("/api/yarn")
@Tag(name = "YARN集群", description = "YARN 集群配置安全摘要查询接口")
public class YarnClustersController {

    private final YarnClusterQueryService yarnClusterQueryService;

    public YarnClustersController(YarnClusterQueryService yarnClusterQueryService) {
        this.yarnClusterQueryService = yarnClusterQueryService;
    }

    /**
     * 查询服务端已配置的 YARN 集群摘要，仅返回配置状态和脱敏认证信息。
     *
     * @return 集群配置摘要响应
     */
    @GetMapping("/clusters")
    @Operation(summary = "查询YARN集群配置摘要", description = "返回服务端白名单集群标识、认证类型和敏感配置是否已配置，不返回原始路径或凭据。")
    public YarnClustersResponse clusters() {
        return yarnClusterQueryService.listClusters();
    }
}
