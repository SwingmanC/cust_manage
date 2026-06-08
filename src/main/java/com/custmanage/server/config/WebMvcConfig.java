package com.custmanage.server.config;

import com.custmanage.server.auth.AuthInterceptor;
import com.custmanage.server.auth.PermissionInterceptor;
import com.custmanage.server.mapper.MenuMapper;
import com.custmanage.server.mapper.RoleMapper;
import com.custmanage.server.service.IAuthService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final IAuthService authService;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;

    public WebMvcConfig(IAuthService authService,
                        RoleMapper roleMapper,
                        MenuMapper menuMapper) {
        this.authService = authService;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器
        registry.addInterceptor(new AuthInterceptor(authService, List.of(
                "/api/auth/login",
                "/api/auth/oauth",
                "/api/health"
        ))).addPathPatterns("/api/**").order(1);

        // 权限拦截器
        registry.addInterceptor(new PermissionInterceptor(roleMapper, menuMapper))
                .addPathPatterns("/api/**").order(2);
    }

}
