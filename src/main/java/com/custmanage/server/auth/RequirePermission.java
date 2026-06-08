package com.custmanage.server.auth;

import java.lang.annotation.*;

/**
 * 接口权限校验注解。标注在 Controller 方法上，PermissionInterceptor 自动校验。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /** 权限标识，如 "group:delete" */
    String value();
}
