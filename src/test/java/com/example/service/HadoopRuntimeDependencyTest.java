/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Hadoop 运行时依赖测试，验证 YarnClient 初始化链路需要的 JAX-RS 2.x 类存在。
 *
 * <p>
 * created on: 2026-05-21
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class HadoopRuntimeDependencyTest {

    /**
     * 验证 Hadoop TimelineUtils 静态初始化依赖的 NoContentException 可被运行时类路径加载。
     */
    @Test
    public void shouldLoadJaxRsNoContentException() {
        assertDoesNotThrow(() -> Class.forName("javax.ws.rs.core.NoContentException"));
    }
}
