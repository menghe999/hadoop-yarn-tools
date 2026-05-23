/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import java.util.ArrayList;
import java.util.List;

/**
 * YARN 节点列表响应体，保留查询条件和 RM 视角下的 NodeManager 节点集合。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class YarnNodesResponse {

    private String clusterId;
    private String state;
    private List<YarnNodeInfo> nodes = new ArrayList<YarnNodeInfo>();

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<YarnNodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<YarnNodeInfo> nodes) {
        this.nodes = nodes;
    }
}
