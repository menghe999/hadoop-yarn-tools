/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.logaggregation.ContainerLogsRequest;

import java.io.IOException;

/**
 * YARN 聚合日志拉取执行器抽象，便于生产复用 LogCLIHelpers 并在单元测试中替换为 Mock。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public interface YarnLogsDumper {

    /**
     * 按指定 Hadoop 配置和容器日志请求拉取 YARN 聚合日志。
     *
     * @param configuration Hadoop 客户端配置
     * @param request 容器日志请求
     * @return Hadoop 日志拉取返回码，0 表示成功
     * @throws IOException 拉取日志时发生的 IO 异常
     */
    int dump(Configuration configuration, ContainerLogsRequest request) throws IOException;
}
