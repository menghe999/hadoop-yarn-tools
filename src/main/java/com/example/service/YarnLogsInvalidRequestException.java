/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

/**
 * YARN 日志下载请求参数异常，用于将未知集群、非法应用 ID 等客户端错误映射为 400 响应。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnLogsInvalidRequestException extends RuntimeException {

    /**
     * 构造请求参数异常。
     *
     * @param message 异常说明
     */
    public YarnLogsInvalidRequestException(String message) {
        super(message);
    }
}
