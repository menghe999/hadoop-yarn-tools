/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

/**
 * YARN 应用列表查询异常，用于将 ResourceManager 查询失败映射为网关类响应。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnApplicationsQueryException extends RuntimeException {

    /**
     * 构造应用查询异常。
     *
     * @param message 异常说明
     * @param cause 原始异常
     */
    public YarnApplicationsQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
