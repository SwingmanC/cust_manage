package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.service.IDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IDashboardService dashboardService;

    public DashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** 按时间统计 */
    @GetMapping("/time")
    public ApiResponse<Map<String, Object>> timeView(@RequestParam String month) {
        return ApiResponse.ok(dashboardService.getTimeView(month));
    }

    /** 按 BU 统计 */
    @GetMapping("/bu")
    public ApiResponse<Map<String, Object>> buView(@RequestParam Long buOrgId) {
        return ApiResponse.ok(dashboardService.getBuView(buOrgId));
    }

    /** BU 下拉列表 */
    @GetMapping("/bu-options")
    public ApiResponse<List<Map<String, Object>>> buOptions() {
        return ApiResponse.ok(dashboardService.getBuOptions());
    }

    /** 客户经理产能排名 */
    @GetMapping("/manager-ranking")
    public ApiResponse<Map<String, Object>> managerRanking(@RequestParam String month) {
        return ApiResponse.ok(dashboardService.getManagerRanking(month));
    }
}
