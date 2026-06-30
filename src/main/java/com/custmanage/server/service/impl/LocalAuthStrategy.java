package com.custmanage.server.service.impl;

import com.custmanage.server.auth.PasswordCodec;
import com.custmanage.server.auth.context.LoginUser;
import com.custmanage.server.common.BusinessException;
import com.custmanage.server.mapper.RoleMapper;
import com.custmanage.server.mapper.UserMapper;
import com.custmanage.server.service.AuthStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地账号密码认证。密码校验委托 {@link PasswordCodec}（MD5(盐+密码)）。
 */
@Component
public class LocalAuthStrategy implements AuthStrategy {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordCodec passwordCodec;

    public LocalAuthStrategy(UserMapper userMapper, RoleMapper roleMapper, PasswordCodec passwordCodec) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordCodec = passwordCodec;
    }

    @Override
    public LoginUser authenticate(String account, String password) {
        LoginUser user = userMapper.selectByOaAccount(account);
        if (user == null) {
            throw new BusinessException(40004, "账号或密码错误");
        }

        // 校验账号状态
        if ("停用".equals(user.getAccountStatus())) {
            throw new BusinessException(40001, "账号已停用，请联系管理员");
        }
        if ("锁定".equals(user.getAccountStatus())) {
            throw new BusinessException(40001, "账号已锁定，请联系管理员");
        }

        // 校验密码：MD5(盐 + 密码)
        if (!passwordCodec.matches(password, user.getPasswordHash())) {
            throw new BusinessException(40002, "账号或密码错误");
        }

        // 加载角色
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getUserId());
        user.setRoles(roles);

        // 清除敏感信息
        user.setPasswordHash(null);

        return user;
    }

    @Override
    public AuthType supportedType() {
        return AuthType.LOCAL;
    }
}
