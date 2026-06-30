package com.custmanage.server.service;

import com.custmanage.server.auth.DataScopeContext;
import com.custmanage.server.vo.LoginResponse;
import com.custmanage.server.vo.PermissionLoadResponse;
import io.jsonwebtoken.Claims;

/**
 * 认证服务接口。
 */
public interface IAuthService {

    /** 登录 */
    LoginResponse login(String account, String password, String ip);

    /** 生成 JWT Token */
    String generateToken(com.custmanage.server.auth.context.LoginUser user);

    /** 解析 JWT Token */
    Claims parseToken(String token);

    /** 加载用户权限（菜单、按钮、数据范围） */
    PermissionLoadResponse loadPermissions(Long userId);

    /** 修改当前登录用户的密码 */
    void changePassword(String oldPassword, String newPassword);
}
