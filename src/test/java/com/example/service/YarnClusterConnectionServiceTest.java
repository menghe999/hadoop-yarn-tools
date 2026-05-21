/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * YARN 集群连接服务单元测试，验证 simple 模式也会进入统一认证边界并显式设置 Hadoop 认证模式。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnClusterConnectionServiceTest {

    /**
     * 验证非 Kerberos 集群操作执行前会显式设置 simple 认证，避免继承此前 Kerberos 全局状态。
     *
     * @throws Exception 执行集群操作异常
     */
    @Test
    public void shouldSetSimpleAuthenticationBeforeSimpleClusterOperation() throws Exception {
        YarnClusterConnectionService service = new YarnClusterConnectionService();
        YarnClusterProperties.Cluster cluster = new YarnClusterProperties.Cluster();
        Configuration configuration = new Configuration();

        String authentication = service.execute(cluster, configuration, () ->
                configuration.get("hadoop.security.authentication"));

        assertEquals("simple", authentication);
    }

    /**
     * 验证 simple 操作会重建登录用户，避免复用此前留在 JVM 全局状态中的 Kerberos 用户。
     *
     * @throws Exception 执行集群操作异常
     */
    @Test
    public void shouldResetPreviousLoginUserBeforeSimpleClusterOperation() throws Exception {
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("stale-kerberos-user"));
        YarnClusterConnectionService service = new YarnClusterConnectionService();
        YarnClusterProperties.Cluster cluster = new YarnClusterProperties.Cluster();
        Configuration configuration = new Configuration();

        String loginUser = service.execute(cluster, configuration, () ->
                UserGroupInformation.getLoginUser().getUserName());

        assertNotEquals("stale-kerberos-user", loginUser);
    }
}
