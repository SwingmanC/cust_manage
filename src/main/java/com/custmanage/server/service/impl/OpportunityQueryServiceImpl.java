package com.custmanage.server.service.impl;

import com.custmanage.server.mapper.OpportunityMapper;
import com.custmanage.server.service.IOpportunityQueryService;
import com.custmanage.server.vo.OpportunityVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpportunityQueryServiceImpl implements IOpportunityQueryService {
    private final OpportunityMapper opportunityMapper;

    public OpportunityQueryServiceImpl(OpportunityMapper opportunityMapper) {
        this.opportunityMapper = opportunityMapper;
    }

    @Override
    public List<OpportunityVO> queryOpportunities(Long groupId, String status, String type, String sourceType) {
        return opportunityMapper.selectOpportunities(groupId, status, type, sourceType);
    }
}
