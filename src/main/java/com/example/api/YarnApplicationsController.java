/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.YarnApplicationsQueryException;
import com.example.service.YarnApplicationsQueryService;
import com.example.service.YarnLogsInvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

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
public class YarnApplicationsController {

    private final YarnApplicationsQueryService yarnApplicationsQueryService;

    public YarnApplicationsController(YarnApplicationsQueryService yarnApplicationsQueryService) {
        this.yarnApplicationsQueryService = yarnApplicationsQueryService;
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
}
