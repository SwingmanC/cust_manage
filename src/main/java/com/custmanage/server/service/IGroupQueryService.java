package com.custmanage.server.service;

import com.custmanage.server.common.PageResponse;
import com.custmanage.server.dto.GroupQueryRequest;
import com.custmanage.server.vo.GroupDetailVO;
import com.custmanage.server.vo.GroupListVO;

public interface IGroupQueryService {
    PageResponse<GroupListVO> queryGroups(GroupQueryRequest request);

    GroupDetailVO getGroupDetail(Long groupId);
}
