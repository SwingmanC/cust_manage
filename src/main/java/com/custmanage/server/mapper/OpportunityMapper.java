package com.custmanage.server.mapper;

import com.custmanage.server.vo.OpportunityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OpportunityMapper {
    List<OpportunityVO> selectOpportunities(@Param("groupId") Long groupId,
                                            @Param("status") String status,
                                            @Param("type") String type,
                                            @Param("sourceType") String sourceType);
}
