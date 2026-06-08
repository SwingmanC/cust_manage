package com.custmanage.server.service.impl;

import com.custmanage.server.auth.context.LoginUser;
import com.custmanage.server.common.BusinessException;
import com.custmanage.server.mapper.RoleMapper;
import com.custmanage.server.mapper.UserMapper;
import com.custmanage.server.service.AuthStrategy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 本地账号密码认证。密码加密方式：MD5(盐 + 密码)。
 */
@Component
public class LocalAuthStrategy implements AuthStrategy {

    private static final String PASSWORD_SALT = "cust_manage_salt";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public LocalAuthStrategy(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
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

        // 校验密码: MD5(盐 + 密码)
        String inputHash = md5(PASSWORD_SALT + password);
        if (!inputHash.equalsIgnoreCase(user.getPasswordHash())) {
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

    static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
