package com.custmanage.server.service;

import com.custmanage.server.auth.context.LoginUser;

/**
 * 认证策略接口，预留 OA/OAuth2 扩展。
 */
public interface AuthStrategy {

    LoginUser authenticate(String account, String password);

    AuthType supportedType();

    enum AuthType {
        LOCAL, OA, OAUTH2
    }
}
