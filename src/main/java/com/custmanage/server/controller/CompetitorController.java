package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.common.PageResponse;
import com.custmanage.server.service.ICompetitorService;
import com.custmanage.server.vo.CompetitorVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/competitors")
public class CompetitorController {

    private final ICompetitorService competitorService;

    public CompetitorController(ICompetitorService competitorService) {
        this.competitorService = competitorService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CompetitorVO>> list(
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "active") String tab,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(competitorService.queryList(groupId, tab, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<CompetitorVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(competitorService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        return ApiResponse.ok(competitorService.create(params));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        return ApiResponse.ok(competitorService.update(id, params));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        return ApiResponse.ok(competitorService.delete(id));
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<Map<String, Object>>> history(@PathVariable Long id) {
        return ApiResponse.ok(competitorService.getHistory(id));
    }

    @GetMapping("/ticket/{ticketId}")
    public ApiResponse<Map<String, Object>> ticketDetail(@PathVariable Long ticketId) {
        return ApiResponse.ok(competitorService.getTicketDetail(ticketId));
    }

    @GetMapping("/ticket/by-competitor/{competitorId}")
    public ApiResponse<Map<String, Object>> ticketByCompetitor(@PathVariable Long competitorId) {
        return ApiResponse.ok(competitorService.getTicketByCompetitorId(competitorId));
    }

    @PostMapping("/ticket/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        competitorService.approveTicket(id, body.getOrDefault("comment", ""));
        return ApiResponse.ok(null);
    }

    @PostMapping("/ticket/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        if (reason.trim().isEmpty()) reason = "驳回";
        competitorService.rejectTicket(id, reason);
        return ApiResponse.ok(null);
    }
}
