/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import java.nio.file.Path;

/**
 * 已拉取到服务端临时目录的 YARN 日志下载结果，供控制器选择单文件或 ZIP 流式响应。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public class DownloadedYarnLogs {

    private final Path temporaryDirectory;

    private final Path contentPath;

    private final boolean zipRequired;

    private final String fileName;

    /**
     * 创建 YARN 日志下载结果。
     *
     * @param temporaryDirectory 服务端受控临时目录
     * @param contentPath 单文件下载路径或待打包目录路径
     * @param zipRequired 是否需要打包为 ZIP
     * @param fileName 响应下载文件名
     */
    public DownloadedYarnLogs(Path temporaryDirectory, Path contentPath, boolean zipRequired, String fileName) {
        this.temporaryDirectory = temporaryDirectory;
        this.contentPath = contentPath;
        this.zipRequired = zipRequired;
        this.fileName = fileName;
    }

    public Path getTemporaryDirectory() {
        return temporaryDirectory;
    }

    public Path getContentPath() {
        return contentPath;
    }

    public boolean isZipRequired() {
        return zipRequired;
    }

    public String getFileName() {
        return fileName;
    }
}
