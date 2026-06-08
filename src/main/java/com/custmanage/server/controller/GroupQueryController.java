package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.common.PageResponse;
import com.custmanage.server.dto.GroupQueryRequest;
import com.custmanage.server.service.IGroupQueryService;
import com.custmanage.server.vo.GroupDetailVO;
import com.custmanage.server.vo.GroupListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
public class GroupQueryController {
    private final IGroupQueryService groupQueryService;

    public GroupQueryController(IGroupQueryService groupQueryService) {
        this.groupQueryService = groupQueryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<GroupListVO>> queryGroups(@ModelAttribute GroupQueryRequest request) {
        return ApiResponse.ok(groupQueryService.queryGroups(request));
    }

    @GetMapping("/{groupId}")
    public ApiResponse<GroupDetailVO> getGroupDetail(@PathVariable Long groupId) {
        return ApiResponse.ok(groupQueryService.getGroupDetail(groupId));
    }
}
