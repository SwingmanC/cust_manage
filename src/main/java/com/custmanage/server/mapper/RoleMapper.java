package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    /** 查询用户绑定的角色编码列表 */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /** 根据角色编码查询角色 ID 列表 */
    List<Long> selectRoleIdsByCodes(@Param("roleCodes") List<String> roleCodes);
}
