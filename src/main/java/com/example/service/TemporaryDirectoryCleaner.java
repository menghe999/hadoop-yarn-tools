/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端临时目录清理工具，确保下载完成、失败或业务异常时不遗留 YARN 日志文件。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public final class TemporaryDirectoryCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryDirectoryCleaner.class);

    private TemporaryDirectoryCleaner() {
    }

    /**
     * 递归删除临时目录；清理失败时抛出业务异常，避免静默吞掉服务端磁盘问题。
     *
     * @param directory 待删除目录
     */
    public static void deleteRecursively(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        throw exc;
                    }
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new YarnLogsDownloadException("清理YARN日志临时目录失败: " + directory, e);
        }
    }

    /**
     * 尽力清理临时目录；用于响应流结束或异常处理阶段，避免清理失败覆盖真实业务结果。
     *
     * @param directory 待删除目录
     */
    public static void deleteQuietly(Path directory) {
        try {
            deleteRecursively(directory);
        } catch (YarnLogsDownloadException e) {
            LOGGER.warn("清理YARN日志临时目录失败: {}", directory, e);
        }
    }
}
