/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

/**
 * YARN 日志下载业务异常，用于将配置加载、Kerberos 登录和日志拉取失败映射为非 2xx HTTP 响应。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnLogsDownloadException extends RuntimeException {

    /**
     * 构造带业务说明的日志下载异常。
     *
     * @param message 异常说明
     */
    public YarnLogsDownloadException(String message) {
        super(message);
    }

    /**
     * 构造带根因的日志下载异常，保留底层 Hadoop 或文件系统异常信息。
     *
     * @param message 异常说明
     * @param cause 根因异常
     */
    public YarnLogsDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
