package com.custmanage.server.mapper;

import com.custmanage.server.vo.CompetitorVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CompetitorMapper {

    List<CompetitorVO> selectList(@Param("groupIds") List<Long> groupIds,
                                  @Param("status") String status,
                                  @Param("statusList") List<String> statusList,
                                  @Param("keyword") String keyword);

    CompetitorVO selectById(@Param("id") Long id);

    int insert(Map<String, Object> params);

    int update(Map<String, Object> params);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int deleteLogic(@Param("id") Long id);

    long countList(@Param("groupIds") List<Long> groupIds,
                   @Param("status") String status,
                   @Param("statusList") List<String> statusList);

    /** 历史变更 */
    List<Map<String, Object>> selectHistory(@Param("competitorId") Long competitorId);
}
