package com.custmanage.server.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    public ApiResponse(@JsonProperty("code") int code,
                       @JsonProperty("message") String message,
                       @JsonProperty("data") T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @JsonProperty("code")
    public int code() {
        return code;
    }

    @JsonProperty("message")
    public String message() {
        return message;
    }

    @JsonProperty("data")
    public T data() {
        return data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>(0, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<T>(code, message, null);
    }
}
