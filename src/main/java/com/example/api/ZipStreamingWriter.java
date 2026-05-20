/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.FileNameCleaner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * YARN 日志目录 ZIP 流式写出器，负责清洗 ZIP entry 名称以避免 Zip Slip 风险。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
public final class ZipStreamingWriter {

    private static final int BUFFER_SIZE = 8192;

    private ZipStreamingWriter() {
    }

    /**
     * 将指定目录中的普通文件流式写入 ZIP 输出流，文件条目名按相对路径逐段清洗。
     *
     * @param directory 待压缩目录
     * @param outputStream HTTP 响应输出流
     * @throws IOException 文件读取或 ZIP 写出异常
     */
    public static void writeDirectory(Path directory, OutputStream outputStream) throws IOException {
        final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        List<Path> files = collectRegularFiles(directory);
        for (Path file : files) {
            writeFile(directory, file, zipOutputStream);
        }
        zipOutputStream.finish();
        zipOutputStream.flush();
    }

    private static List<Path> collectRegularFiles(Path directory) throws IOException {
        final List<Path> files = new ArrayList<Path>();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        Collections.sort(files);
        return files;
    }

    private static void writeFile(Path rootDirectory, Path file, ZipOutputStream zipOutputStream) throws IOException {
        String entryName = buildSafeEntryName(rootDirectory.relativize(file));
        if (entryName.length() == 0) {
            return;
        }
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        InputStream inputStream = Files.newInputStream(file);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
            zipOutputStream.closeEntry();
        }
    }

    private static String buildSafeEntryName(Path relativePath) {
        StringBuilder builder = new StringBuilder();
        for (Path segment : relativePath) {
            String safeSegment = FileNameCleaner.clean(segment.toString());
            if (safeSegment.length() == 0 || ".".equals(safeSegment)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(safeSegment);
        }
        return builder.toString();
    }
}
