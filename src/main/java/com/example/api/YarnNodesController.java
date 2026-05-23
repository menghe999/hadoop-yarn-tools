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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * YARN 节点控制器，提供 ResourceManager 视角下的 NodeManager 节点查询接口。
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
@Tag(name = "YARN节点", description = "YARN NodeManager 节点查询接口")
@Validated
public class YarnNodesController {

    private final YarnRuntimeQueryService yarnRuntimeQueryService;

    public YarnNodesController(YarnRuntimeQueryService yarnRuntimeQueryService) {
        this.yarnRuntimeQueryService = yarnRuntimeQueryService;
    }

    /**
     * 查询 NodeManager 节点列表；state 为空时查询所有状态。
     *
     * @param clusterId 服务端白名单集群标识
     * @param state 可选节点状态
     * @return 节点列表响应
     */
    @GetMapping("/nodes")
    @Operation(summary = "查询YARN节点列表", description = "返回ResourceManager视角下的NodeManager节点状态、资源和健康信息。")
    public YarnNodesResponse nodes(
            @RequestParam("clusterId")
            @NotBlank(message = "clusterId不能为空")
            @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确") String clusterId,
            @RequestParam(value = "state", required = false)
            @Pattern(regexp = "|[A-Za-z_]{1,32}", message = "state格式不正确") String state) {
        try {
            return yarnRuntimeQueryService.listNodes(clusterId, state);
        } catch (YarnLogsInvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (YarnApplicationsQueryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YARN节点查询失败", e);
        }
    }

    /**
     * 从节点列表结果中过滤单个 NodeManager 节点详情。
     *
     * @param nodeId YARN 节点 ID
     * @param clusterId 服务端白名单集群标识
     * @return 节点详情响应
     */
    @GetMapping("/nodes/{nodeId}")
    @Operation(summary = "查询YARN节点详情", description = "按nodeId从ResourceManager节点列表中过滤并返回单个节点详情。")
    public YarnNodeInfo nodeDetail(
            @PathVariable("nodeId")
            @Pattern(regexp = "[A-Za-z0-9._:-]{1,256}", message = "nodeId格式不正确") String nodeId,
            @RequestParam("clusterId")
            @NotBlank(message = "clusterId不能为空")
            @Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "clusterId格式不正确") String clusterId) {
        try {
            return yarnRuntimeQueryService.getNodeDetail(clusterId, nodeId);
        } catch (YarnLogsInvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (YarnApplicationsQueryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YARN节点详情查询失败", e);
        }
    }
}
