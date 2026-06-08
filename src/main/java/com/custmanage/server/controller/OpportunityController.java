package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.service.IOpportunityQueryService;
import com.custmanage.server.vo.OpportunityVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
public class OpportunityController {
    private final IOpportunityQueryService opportunityQueryService;

    public OpportunityController(IOpportunityQueryService opportunityQueryService) {
        this.opportunityQueryService = opportunityQueryService;
    }

    @GetMapping("/opportunities")
    public ApiResponse<List<OpportunityVO>> queryOpportunities(@PathVariable Long groupId,
                                                                @RequestParam(required = false) String status,
                                                                @RequestParam(required = false) String type,
                                                                @RequestParam(required = false) String sourceType) {
        return ApiResponse.ok(opportunityQueryService.queryOpportunities(groupId, status, type, sourceType));
    }
}
