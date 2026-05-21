/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger OpenAPI 配置，聚合 YARN Web 接口供 Swagger UI 调试调用。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * 配置 OpenAPI 基础信息，便于 Swagger UI 展示服务用途和版本。
     *
     * @return OpenAPI 描述对象
     */
    @Bean
    public OpenAPI yarnToolsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Hadoop YARN Tools API")
                .version("1.1.0")
                .description("YARN 聚合日志下载和队列应用查询接口"));
    }

    /**
     * 只暴露 YARN 相关接口到 yarn 分组，避免 Swagger 文档混入非业务路径。
     *
     * @return YARN 接口分组
     */
    @Bean
    public GroupedOpenApi yarnOpenApiGroup() {
        return GroupedOpenApi.builder()
                .group("yarn")
                .pathsToMatch("/api/yarn/**")
                .build();
    }
}
