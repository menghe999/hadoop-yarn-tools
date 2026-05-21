/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

/**
 * YARN 集群连接上下文中的回调操作，用于统一保护 Kerberos 登录后的 Hadoop API 调用边界。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public interface YarnClusterOperation<T> {

    /**
     * 在已经加载 Hadoop 配置并完成必要认证的上下文中执行集群操作。
     *
     * @return 操作结果
     * @throws Exception Hadoop 或业务执行异常
     */
    T execute() throws Exception;
}
