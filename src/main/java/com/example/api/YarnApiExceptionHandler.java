/* Copyright (c) 2026. Bangsun Technology.Co.Ltd. All rights reserved. http://www.bsfit.com.cn */
package com.example.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * YARN API 参数异常处理器，将路径和查询参数 Bean Validation 失败统一映射为客户端错误。
 *
 * <p>
 * created on: 2026-05-22
 * </p>
 *
 * @author mengh
 * @since 1.1.0
 */
@RestControllerAdvice(assignableTypes = {
        YarnApplicationsController.class, YarnQueuesController.class, YarnNodesController.class})
public class YarnApiExceptionHandler {

    /**
     * 处理路径变量和查询参数校验异常。
     *
     * @param exception 参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleConstraintViolation(ConstraintViolationException exception) {
    }
}
