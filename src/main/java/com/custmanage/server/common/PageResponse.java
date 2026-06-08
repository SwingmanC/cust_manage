package com.custmanage.server.common;

public record PageResponse<T>(long total, java.util.List<T> records) {
}
