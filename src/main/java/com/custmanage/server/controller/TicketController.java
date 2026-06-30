package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.service.ITicketService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final ITicketService ticketService;

    public TicketController(ITicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** 工单详情 */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(ticketService.getTicketDetail(id));
    }

    /** 审核通过 */
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id,
                                      @RequestBody Map<String, String> body) {
        ticketService.approveTicket(id, body.getOrDefault("comment", ""));
        return ApiResponse.ok(null);
    }

    /** 按业务对象查工单 */
    @GetMapping("/by-contact/{contactId}")
    public ApiResponse<Map<String, Object>> byContactId(@PathVariable Long contactId) {
        return ApiResponse.ok(ticketService.getTicketByContactId(contactId));
    }

    /** 审核驳回 */
    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        if (reason.trim().isEmpty()) {
            reason = "驳回";
        }
        ticketService.rejectTicket(id, reason);
        return ApiResponse.ok(null);
    }
}
