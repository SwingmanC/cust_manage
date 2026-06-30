package com.custmanage.server.controller;

import com.custmanage.server.auth.context.LoginUser;
import com.custmanage.server.auth.context.RequestContext;
import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.dto.ChangePasswordRequest;
import com.custmanage.server.dto.LoginRequest;
import com.custmanage.server.service.IAuthService;
import com.custmanage.server.vo.LoginResponse;
import com.custmanage.server.vo.PermissionLoadResponse;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        LoginResponse response = authService.login(request.account(), request.password(), ip);
        return ApiResponse.ok(response);
    }

    @GetMapping("/menus")
    public ApiResponse<PermissionLoadResponse> loadMenus() {
        Long userId = RequestContext.currentUserId();
        PermissionLoadResponse response = authService.loadPermissions(userId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/user-info")
    public ApiResponse<LoginUser> userInfo() {
        return ApiResponse.ok(RequestContext.get());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok(null);
    }

    /** 修改当前登录用户密码 */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.oldPassword(), request.newPassword());
        return ApiResponse.ok(null);
    }

    @PostMapping("/oauth/callback")
    public ApiResponse<String> oauthCallback() {
        return ApiResponse.error(404, "OA 登录暂未启用");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }
}
