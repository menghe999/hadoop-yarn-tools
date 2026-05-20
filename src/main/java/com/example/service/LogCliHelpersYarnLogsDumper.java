/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.logaggregation.ContainerLogsRequest;
import org.apache.hadoop.yarn.logaggregation.LogCLIHelpers;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 基于 Hadoop {@link LogCLIHelpers} 的生产日志拉取执行器，复用现有 CLI 的核心调用链。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@Component
public class LogCliHelpersYarnLogsDumper implements YarnLogsDumper {

    /**
     * 使用 LogCLIHelpers.dumpAllContainersLogs 拉取指定应用的全部容器聚合日志。
     *
     * @param configuration Hadoop 客户端配置
     * @param request 容器日志请求
     * @return Hadoop 日志拉取返回码，0 表示成功
     * @throws IOException 拉取日志时发生的 IO 异常
     */
    @Override
    public int dump(Configuration configuration, ContainerLogsRequest request) throws IOException {
        LogCLIHelpers logCliHelpers = new LogCLIHelpers();
        logCliHelpers.setConf(configuration);
        return logCliHelpers.dumpAllContainersLogs(request);
    }
}
