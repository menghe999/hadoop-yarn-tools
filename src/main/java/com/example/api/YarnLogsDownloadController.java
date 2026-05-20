/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import com.example.service.DownloadedYarnLogs;
import com.example.service.TemporaryDirectoryCleaner;
import com.example.service.YarnLogsDownloadException;
import com.example.service.YarnLogsDownloadService;
import com.example.service.YarnLogsInvalidRequestException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * YARN 聚合日志下载控制器，提供受控临时目录输出和流式文件响应能力。
 *
 * <p>
 * created on: 2026-05-20
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@RestController
@RequestMapping("/api/yarn/logs")
public class YarnLogsDownloadController {

    private static final int BUFFER_SIZE = 8192;

    private final YarnLogsDownloadService yarnLogsDownloadService;

    public YarnLogsDownloadController(YarnLogsDownloadService yarnLogsDownloadService) {
        this.yarnLogsDownloadService = yarnLogsDownloadService;
    }

    /**
     * 下载指定 YARN 应用聚合日志；单文件直接返回，多文件或目录以 ZIP 流式返回。
     *
     * @param request 日志下载请求参数
     * @return 流式下载响应
     */
    @PostMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@Valid @RequestBody YarnLogsDownloadRequest request) {
        final DownloadedYarnLogs downloadedYarnLogs;
        try {
            downloadedYarnLogs = yarnLogsDownloadService.download(request);
        } catch (YarnLogsInvalidRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (YarnLogsDownloadException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YARN日志下载失败", e);
        }

        StreamingResponseBody responseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                try {
                    if (downloadedYarnLogs.isZipRequired()) {
                        ZipStreamingWriter.writeDirectory(downloadedYarnLogs.getContentPath(), outputStream);
                    } else {
                        writeFile(downloadedYarnLogs, outputStream);
                    }
                } finally {
                    TemporaryDirectoryCleaner.deleteQuietly(downloadedYarnLogs.getTemporaryDirectory());
                }
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(downloadedYarnLogs.getFileName()).build());
        MediaType mediaType = downloadedYarnLogs.isZipRequired()
                ? MediaType.parseMediaType("application/zip")
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().headers(headers).contentType(mediaType).body(responseBody);
    }

    private void writeFile(DownloadedYarnLogs downloadedYarnLogs, OutputStream outputStream) throws IOException {
        InputStream inputStream = Files.newInputStream(downloadedYarnLogs.getContentPath());
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
        }
    }
}
