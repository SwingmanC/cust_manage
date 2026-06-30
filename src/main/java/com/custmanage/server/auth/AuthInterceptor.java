package com.custmanage.server.auth;

import com.custmanage.server.auth.context.LoginUser;
import com.custmanage.server.auth.context.RequestContext;
import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.service.IAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证拦截器。校验 Token 并注入用户上下文。
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final IAuthService authService;
    private final List<String> whiteList;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthInterceptor(IAuthService authService, List<String> whiteList) {
        this.authService = authService;
        this.whiteList = whiteList;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // 白名单放行
        for (String white : whiteList) {
            if (path.startsWith(white)) {
                return true;
            }
        }

        // 提取 Token
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        String token = header.substring(BEARER_PREFIX.length());

        try {
            Claims claims = authService.parseToken(token);
            String userId = claims.getSubject();
            String userName = claims.get("userName", String.class);
            String oaAccount = claims.get("oaAccount", String.class);
            Long orgId = claims.get("orgId", Long.class);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            LoginUser user = new LoginUser();
            user.setUserId(Long.valueOf(userId));
            user.setUserName(userName);
            user.setOaAccount(oaAccount);
            user.setOrgId(orgId);
            user.setRoles(roles);
            RequestContext.set(user);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, "Token 无效或已过期");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        RequestContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> error = ApiResponse.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
