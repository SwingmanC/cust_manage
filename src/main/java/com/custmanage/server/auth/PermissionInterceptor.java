package com.custmanage.server.auth;

import com.custmanage.server.auth.context.RequestContext;
import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.mapper.MenuMapper;
import com.custmanage.server.mapper.RoleMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 菜单/按钮/接口权限拦截器。
 * 校验 @RequirePermission 注解的方法。
 */
public class PermissionInterceptor implements HandlerInterceptor {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PermissionInterceptor(RoleMapper roleMapper, MenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            return true;
        }

        String requiredPermission = annotation.value();
        List<String> roleCodes = RequestContext.currentRoles();
        if (roleCodes.isEmpty()) {
            writeForbidden(response, "无权限访问");
            return false;
        }

        List<String> userPermissions = menuMapper.selectPermissionCodesByRoleCodes(roleCodes);
        if (userPermissions.contains(requiredPermission)) {
            return true;
        }

        writeForbidden(response, "无权限访问");
        return false;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> error = ApiResponse.error(403, message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
