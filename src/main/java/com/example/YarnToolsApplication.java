/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Hadoop YARN 工具的 Spring Boot HTTP 入口，保留原有 CLI 类并新增 Web 调用方式。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class YarnToolsApplication {

    /**
     * 启动 Spring Boot Web 应用，用于暴露受控的 YARN 日志下载接口。
     *
     * @param args Spring Boot 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(YarnToolsApplication.class, args);
    }
}
