/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.service;

/**
 * 下载文件名与 ZIP 条目名称清洗工具，防止路径穿越和危险控制字符进入响应头或压缩包。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public final class FileNameCleaner {

    private FileNameCleaner() {
    }

    /**
     * 清洗单段文件名，只保留安全字符并移除目录分隔符。
     *
     * @param fileName 原始文件名
     * @return 安全文件名
     */
    public static String clean(String fileName) {
        if (fileName == null) {
            return "download";
        }
        String cleaned = fileName.replace('\\', '_').replace('/', '_').replace(':', '_');
        cleaned = cleaned.replaceAll("[^A-Za-z0-9._-]", "_");
        while (cleaned.contains("..")) {
            cleaned = cleaned.replace("..", "_");
        }
        if (cleaned.length() == 0 || ".".equals(cleaned)) {
            return "download";
        }
        return cleaned;
    }
}
