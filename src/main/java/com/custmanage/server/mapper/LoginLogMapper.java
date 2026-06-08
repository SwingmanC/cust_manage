package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginLogMapper {

    int insert(@Param("userId") Long userId,
               @Param("loginAccount") String loginAccount,
               @Param("loginIp") String loginIp,
               @Param("loginResult") String loginResult,
               @Param("failReason") String failReason);
}
