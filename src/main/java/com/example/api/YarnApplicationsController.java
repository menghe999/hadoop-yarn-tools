/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.YarnApplicationsQueryException;
import com.example.service.YarnApplicationsQueryService;
import com.example.service.YarnLogsInvalidRequestException;
import com.example.service.YarnRuntimeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * YARN 应用列表查询控制器，提供按队列和状态过滤的只读查询接口。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@RestController
@RequestMapping("/api/yarn")
@Tag(name = "YARN应用", description = "YARN 队列应用查询接口")
@Validated
public class YarnApplicationsController {

    private final YarnApplicationsQueryService yarnApplicationsQueryService;
    private final YarnRuntimeQueryService yarnRuntimeQueryService;

    public YarnApplicationsController(YarnApplicationsQueryService yarnApplicationsQueryService,
            YarnRuntimeQueryService yarnRuntimeQueryService) {
        this.yarnApplicationsQueryService = yarnApplicationsQueryService;
        this.yarnRuntimeQueryService = yarnRuntimeQueryService;
    }

    /**
     * 查询指定队列下的应用列表；state 为空时返回队列下所有状态的应用。
     *
     * @param request 查询参数
     * @return 应用列表响应
     */
    @GetMapping("/applications")
    @Operation(summary = "查询队列应用列表", description = "按服务端集群标识和队列查询YARN应用，state为空时不按状态过滤。")
    public YarnApplicationsResponse applications(@Valid YarnApplicationsQueryRequest request) {
        try {
            return yarnApplicationsQueryService.query(request);
        } catch (YarnLogsInvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (YarnApplicationsQueryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YARN应用列表查询失败", e);
        }
    }

    /**
     * 查询单个 YARN 应用详情，包括状态、时间线、追踪地址和资源用量。
     *
     * @param applicationId YARN 应用 ID
     * @param clusterId 服务端白名单集群标识
     * @return 应用详情响应
     */
    @GetMapping("/applications/{applicationId}")
    @Operation(summary = "查询YARN应用详情", description = "按服务端集群标识和应用ID查询单个YARN应用详情。")
    public YarnApplicationDetail applicationDetail(
            @PathVariable("applicationId")
            @Pattern(regexp = "application_\\d+_\\d+", message = "applicationId格式不正确") String applicationId,
            @RequestParam("clusterId")
            @NotBlank(message = "clusterId不能为空")
            @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确") String clusterId) {
        try {
            return yarnRuntimeQueryService.getApplicationDetail(clusterId, applicationId);
        } catch (YarnLogsInvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (YarnApplicationsQueryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YARN应用详情查询失败", e);
        }
    }

    /**
     * 提交 YARN 应用 kill 请求；终态应用会拒绝操作并返回客户端错误。
     *
     * @param applicationId YARN 应用 ID
     * @param request kill 请求体
     * @return kill 请求提交结果
     */
    @PostMapping("/applications/{applicationId}/kill")
    @Operation(summary = "终止YARN应用", description = "按服务端集群标识和应用ID提交kill请求；该操作会终止非终态应用。")
    public YarnApplicationKillResponse killApplication(
            @PathVariable("applicationId")
            @Pattern(regexp = "application_\\d+_\\d+", message = "applicationId格式不正确") String applicationId,
            @Valid @RequestBody YarnApplicationKillRequest request) {
        try {
            return yarnRuntimeQueryService.killApplication(request.getClusterId(), applicationId);
        } catch (YarnLogsInvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (YarnApplicationsQueryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YARN应用kill失败", e);
        }
    }
}
