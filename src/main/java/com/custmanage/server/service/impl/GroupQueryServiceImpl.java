package com.custmanage.server.service.impl;

import com.custmanage.server.auth.DataScopeContext;
import com.custmanage.server.auth.context.RequestContext;
import com.custmanage.server.service.IDataScopeService;
import com.custmanage.server.common.PageResponse;
import com.custmanage.server.common.ResourceNotFoundException;
import com.custmanage.server.dto.GroupQueryRequest;
import com.custmanage.server.mapper.GroupMapper;
import com.custmanage.server.service.IGroupQueryService;
import com.custmanage.server.vo.GroupDetailVO;
import com.custmanage.server.vo.GroupListVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupQueryServiceImpl implements IGroupQueryService {

    private final GroupMapper groupMapper;
    private final IDataScopeService dataScopeService;

    public GroupQueryServiceImpl(GroupMapper groupMapper, IDataScopeService dataScopeService) {
        this.groupMapper = groupMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public PageResponse<GroupListVO> queryGroups(GroupQueryRequest request) {
        // 注入数据权限
        applyDataScope(request);

        long total = groupMapper.countGroups(request);
        return new PageResponse<>(total,
                groupMapper.selectGroups(request, request.offset(), request.limit()));
    }

    @Override
    public GroupDetailVO getGroupDetail(Long groupId) {
        GroupDetailVO detail = groupMapper.selectGroupDetail(groupId);
        if (detail == null) {
            throw new ResourceNotFoundException("集团不存在或已删除");
        }
        return detail;
    }

    private void applyDataScope(GroupQueryRequest request) {
        Long userId = RequestContext.currentUserId();
        Long orgId = RequestContext.currentOrgId();
        List<String> roles = RequestContext.currentRoles();

        DataScopeContext scope = dataScopeService.resolveDataScope(userId, orgId, roles);
        request.setDataScope(scope);
    }
}
