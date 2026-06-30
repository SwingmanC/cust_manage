package com.custmanage.server.mapper;

import com.custmanage.server.auth.context.LoginUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    /** 根据 OA 账号查询用户（用于登录认证） */
    LoginUser selectByOaAccount(@Param("oaAccount") String oaAccount);

    /** 根据用户 ID 查询 */
    LoginUser selectById(@Param("userId") Long userId);

    /** 修改密码（更新 password_hash） */
    int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
}
