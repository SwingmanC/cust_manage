package com.custmanage.server.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PageResponse<T> {

    private final long total;
    private final List<T> records;

    public PageResponse(@JsonProperty("total") long total,
                        @JsonProperty("records") List<T> records) {
        this.total = total;
        this.records = records;
    }

    @JsonProperty("total")
    public long total() {
        return total;
    }

    @JsonProperty("records")
    public List<T> records() {
        return records;
    }
}
