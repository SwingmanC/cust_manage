package com.custmanage.server.service;

import com.custmanage.server.vo.OpportunityVO;

import java.util.List;

public interface IOpportunityQueryService {
    List<OpportunityVO> queryOpportunities(Long groupId, String status, String type, String sourceType);
}
