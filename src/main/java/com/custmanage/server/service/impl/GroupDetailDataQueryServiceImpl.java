package com.custmanage.server.service.impl;

import com.custmanage.server.dto.BcQueryRequest;
import com.custmanage.server.dto.CompetitorQueryRequest;
import com.custmanage.server.dto.MonthRangeRequest;
import com.custmanage.server.mapper.GroupDetailDataMapper;
import com.custmanage.server.service.IGroupDetailDataQueryService;
import com.custmanage.server.vo.BcBusinessVO;
import com.custmanage.server.vo.CompetitorVO;
import com.custmanage.server.vo.RevenueMonthlyVO;
import com.custmanage.server.vo.RevenueTrendVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupDetailDataQueryServiceImpl implements IGroupDetailDataQueryService {
    private final GroupDetailDataMapper detailDataMapper;

    public GroupDetailDataQueryServiceImpl(GroupDetailDataMapper detailDataMapper) {
        this.detailDataMapper = detailDataMapper;
    }

    @Override
    public List<RevenueMonthlyVO> queryRevenues(Long groupId, MonthRangeRequest request) {
        return detailDataMapper.selectRevenues(groupId, request);
    }

    @Override
    public List<RevenueTrendVO> queryRevenueTrend(Long groupId, MonthRangeRequest request) {
        return detailDataMapper.selectRevenueTrend(groupId, request);
    }

    @Override
    public List<BcBusinessVO> queryBcBusinesses(Long groupId, BcQueryRequest request) {
        return detailDataMapper.selectBcBusinesses(groupId, request);
    }

    @Override
    public List<CompetitorVO> queryCompetitors(Long groupId, CompetitorQueryRequest request) {
        return detailDataMapper.selectCompetitors(groupId, request);
    }
}
