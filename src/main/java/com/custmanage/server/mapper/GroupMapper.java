package com.custmanage.server.mapper;

import com.custmanage.server.dto.GroupQueryRequest;
import com.custmanage.server.vo.GroupDetailVO;
import com.custmanage.server.vo.GroupListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupMapper {
    long countGroups(@Param("query") GroupQueryRequest query);

    List<GroupListVO> selectGroups(@Param("query") GroupQueryRequest query,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    GroupDetailVO selectGroupDetail(@Param("groupId") Long groupId);

    /** 查询客户经理负责的集团ID列表 */
    List<Long> selectGroupIdsByManager(@Param("managerUserId") Long managerUserId);

    /** 查询指定BU下的集团ID列表 */
    List<Long> selectGroupIdsByOrgId(@Param("orgId") Long orgId);
}
