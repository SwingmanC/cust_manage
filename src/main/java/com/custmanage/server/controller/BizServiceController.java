package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.mapper.BizServiceMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class BizServiceController {

    private final BizServiceMapper bizServiceMapper;

    public BizServiceController(BizServiceMapper bizServiceMapper) {
        this.bizServiceMapper = bizServiceMapper;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(bizServiceMapper.selectAll("启用"));
    }
}
