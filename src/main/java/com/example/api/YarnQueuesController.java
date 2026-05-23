/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.YarnApplicationsQueryException;
import com.example.service.YarnLogsInvalidRequestException;
import com.example.service.YarnRuntimeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * YARN 队列控制器，提供 ResourceManager 运行时队列信息只读查询接口。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@RestController
@RequestMapping("/api/yarn")
@Tag(name = "YARN队列", description = "YARN 队列运行时信息查询接口")
@Validated
public class YarnQueuesController {

    private final YarnRuntimeQueryService yarnRuntimeQueryService;

    public YarnQueuesController(YarnRuntimeQueryService yarnRuntimeQueryService) {
        this.yarnRuntimeQueryService = yarnRuntimeQueryService;
    }

    /**
     * 查询当前 ResourceManager 可见的全部队列和队列配置。
     *
     * @param clusterId 服务端白名单集群标识
     * @return 队列响应
     */
    @GetMapping("/queues")
    @Operation(summary = "查询YARN队列信息", description = "返回ResourceManager运行时队列容量、状态、统计和子队列信息。")
    public YarnQueuesResponse queues(
            @RequestParam("clusterId")
            @NotBlank(message = "clusterId不能为空")
            @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确") String clusterId) {
        try {
            return yarnRuntimeQueryService.listQueues(clusterId);
        } catch (YarnLogsInvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (YarnApplicationsQueryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YARN队列查询失败", e);
        }
    }
}
