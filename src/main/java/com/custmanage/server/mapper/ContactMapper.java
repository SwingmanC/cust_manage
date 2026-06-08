package com.custmanage.server.mapper;

import com.custmanage.server.vo.ContactVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ContactMapper {

    List<ContactVO> selectContacts(@Param("groupIds") List<Long> groupIds,
                                   @Param("status") String status,
                                   @Param("statusList") List<String> statusList,
                                   @Param("keyword") String keyword);

    ContactVO selectById(@Param("id") Long id);

    int insert(Map<String, Object> params);

    int update(Map<String, Object> params);

    /** 仅更新通讯录状态（用于工单审核） */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int deleteLogic(@Param("id") Long id);

    /** 查询通讯录历史变更（通过工单快照回溯） */
    List<Map<String, Object>> selectHistory(@Param("contactId") Long contactId);

    long countContacts(@Param("groupIds") List<Long> groupIds,
                       @Param("status") String status,
                       @Param("statusList") List<String> statusList);
}
