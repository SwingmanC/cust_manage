package com.custmanage.server.mapper;

import com.custmanage.server.dto.BcQueryRequest;
import com.custmanage.server.dto.CompetitorQueryRequest;
import com.custmanage.server.dto.MonthRangeRequest;
import com.custmanage.server.vo.BcBusinessVO;
import com.custmanage.server.vo.CompetitorVO;
import com.custmanage.server.vo.RevenueMonthlyVO;
import com.custmanage.server.vo.RevenueTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupDetailDataMapper {
    List<RevenueMonthlyVO> selectRevenues(@Param("groupId") Long groupId,
                                          @Param("query") MonthRangeRequest query);

    List<RevenueTrendVO> selectRevenueTrend(@Param("groupId") Long groupId,
                                            @Param("query") MonthRangeRequest query);

    List<BcBusinessVO> selectBcBusinesses(@Param("groupId") Long groupId,
                                          @Param("query") BcQueryRequest query);

    List<CompetitorVO> selectCompetitors(@Param("groupId") Long groupId,
                                         @Param("query") CompetitorQueryRequest query);
}
