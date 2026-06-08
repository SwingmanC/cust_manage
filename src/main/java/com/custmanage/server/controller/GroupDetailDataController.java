package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.common.PageResponse;
import com.custmanage.server.dto.BcQueryRequest;
import com.custmanage.server.dto.CompetitorQueryRequest;
import com.custmanage.server.dto.MonthRangeRequest;
import com.custmanage.server.service.IContactService;
import com.custmanage.server.service.IGroupDetailDataQueryService;
import com.custmanage.server.vo.BcBusinessVO;
import com.custmanage.server.vo.CompetitorVO;
import com.custmanage.server.vo.ContactVO;
import com.custmanage.server.vo.RevenueMonthlyVO;
import com.custmanage.server.vo.RevenueTrendVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
public class GroupDetailDataController {

    private final IGroupDetailDataQueryService detailDataQueryService;
    private final IContactService contactService;

    public GroupDetailDataController(IGroupDetailDataQueryService detailDataQueryService,
                                      IContactService contactService) {
        this.detailDataQueryService = detailDataQueryService;
        this.contactService = contactService;
    }

    @GetMapping("/revenues")
    public ApiResponse<List<RevenueMonthlyVO>> queryRevenues(@PathVariable Long groupId,
                                                             @ModelAttribute MonthRangeRequest request) {
        return ApiResponse.ok(detailDataQueryService.queryRevenues(groupId, request));
    }

    @GetMapping("/revenues/trend")
    public ApiResponse<List<RevenueTrendVO>> queryRevenueTrend(@PathVariable Long groupId,
                                                               @ModelAttribute MonthRangeRequest request) {
        return ApiResponse.ok(detailDataQueryService.queryRevenueTrend(groupId, request));
    }

    @GetMapping("/bc")
    public ApiResponse<List<BcBusinessVO>> queryBcBusinesses(@PathVariable Long groupId,
                                                            @ModelAttribute BcQueryRequest request) {
        return ApiResponse.ok(detailDataQueryService.queryBcBusinesses(groupId, request));
    }

    @GetMapping("/competitors")
    public ApiResponse<List<CompetitorVO>> queryCompetitors(@PathVariable Long groupId,
                                                           @ModelAttribute CompetitorQueryRequest request) {
        return ApiResponse.ok(detailDataQueryService.queryCompetitors(groupId, request));
    }

    /** 查询集团下已生效的通讯录 */
    @GetMapping("/contacts")
    public ApiResponse<List<ContactVO>> queryContacts(@PathVariable Long groupId,
                                                       @RequestParam(required = false) String status) {
        String tab = "有效".equals(status) ? "active" : "active";
        PageResponse<ContactVO> page = contactService.queryContacts(groupId, tab, null, 1, 200);
        return ApiResponse.ok(page.records());
    }
}
