/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * YARN 应用查询客户端边界，隔离真实 ResourceManager 访问以便单元测试替换。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public interface YarnApplicationsClient {

    /**
     * 按队列和可选状态查询 YARN 应用列表。
     *
     * @param configuration Hadoop 客户端配置
     * @param queues 队列名称集合
     * @param states 应用状态集合，未过滤时为 null
     * @return 应用报告列表
     * @throws IOException IO 异常
     * @throws YarnException YARN 客户端异常
     */
    List<ApplicationReport> getApplications(
            Configuration configuration, Set<String> queues, EnumSet<YarnApplicationState> states, long limit)
            throws IOException, YarnException;
}
