/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.QueueInfo;
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
     * @param queues 队列名称集合；为 null 时不向 ResourceManager 下推队列过滤
     * @param states 应用状态集合，未指定状态时显式包含所有 YARN 状态
     * @return 应用报告列表
     * @throws IOException IO 异常
     * @throws YarnException YARN 客户端异常
     */
    List<ApplicationReport> getApplications(
            Configuration configuration, Set<String> queues, EnumSet<YarnApplicationState> states, long limit)
            throws IOException, YarnException;

    /**
     * 按应用 ID 查询单个 YARN 应用报告。
     *
     * @param configuration Hadoop 客户端配置
     * @param applicationId YARN 应用 ID
     * @return 应用报告
     * @throws IOException IO 异常
     * @throws YarnException YARN 客户端异常
     */
    ApplicationReport getApplicationReport(Configuration configuration, ApplicationId applicationId)
            throws IOException, YarnException;

    /**
     * 请求 ResourceManager 终止指定 YARN 应用。
     *
     * @param configuration Hadoop 客户端配置
     * @param applicationId YARN 应用 ID
     * @param diagnostics kill 诊断说明
     * @throws IOException IO 异常
     * @throws YarnException YARN 客户端异常
     */
    void killApplication(Configuration configuration, ApplicationId applicationId, String diagnostics)
            throws IOException, YarnException;

    /**
     * 查询当前 ResourceManager 视角下的全部队列运行时信息。
     *
     * @param configuration Hadoop 客户端配置
     * @return 队列信息列表
     * @throws IOException IO 异常
     * @throws YarnException YARN 客户端异常
     */
    List<QueueInfo> getAllQueues(Configuration configuration) throws IOException, YarnException;

    /**
     * 按可选节点状态查询当前 ResourceManager 视角下的 NodeManager 节点报告。
     *
     * @param configuration Hadoop 客户端配置
     * @param states 节点状态过滤条件；为空数组时查询所有状态
     * @return 节点报告列表
     * @throws IOException IO 异常
     * @throws YarnException YARN 客户端异常
     */
    List<NodeReport> getNodeReports(Configuration configuration, NodeState... states) throws IOException, YarnException;
}
