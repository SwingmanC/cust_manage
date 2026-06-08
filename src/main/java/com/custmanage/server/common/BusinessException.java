package com.custmanage.server.common;

/**
 * 通用业务异常，GlobalExceptionHandler 统一处理。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
