package com.custmanage.server.service;

import com.custmanage.server.dto.BcQueryRequest;
import com.custmanage.server.dto.CompetitorQueryRequest;
import com.custmanage.server.dto.MonthRangeRequest;
import com.custmanage.server.vo.BcBusinessVO;
import com.custmanage.server.vo.CompetitorVO;
import com.custmanage.server.vo.RevenueMonthlyVO;
import com.custmanage.server.vo.RevenueTrendVO;

import java.util.List;

public interface IGroupDetailDataQueryService {
    List<RevenueMonthlyVO> queryRevenues(Long groupId, MonthRangeRequest request);

    List<RevenueTrendVO> queryRevenueTrend(Long groupId, MonthRangeRequest request);

    List<BcBusinessVO> queryBcBusinesses(Long groupId, BcQueryRequest request);

    List<CompetitorVO> queryCompetitors(Long groupId, CompetitorQueryRequest request);
}
