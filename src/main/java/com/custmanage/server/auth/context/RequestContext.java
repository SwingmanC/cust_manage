package com.custmanage.server.auth.context;

/**
 * 使用 ThreadLocal 保存当前请求的用户上下文。
 * 每次请求结束后由拦截器清理。
 */
public final class RequestContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    /** 快捷获取当前用户 ID */
    public static Long currentUserId() {
        LoginUser user = get();
        return user != null ? user.getUserId() : null;
    }

    /** 快捷获取当前用户所属组织 ID */
    public static Long currentOrgId() {
        LoginUser user = get();
        return user != null ? user.getOrgId() : null;
    }

    /** 快捷获取当前用户角色列表 */
    public static java.util.List<String> currentRoles() {
        LoginUser user = get();
        return user != null ? user.getRoles() : java.util.Collections.emptyList();
    }
}
