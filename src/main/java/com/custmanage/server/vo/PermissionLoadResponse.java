package com.custmanage.server.vo;

import java.util.List;

/**
 * 登录后加载权限的返回对象。
 */
public class PermissionLoadResponse {

    private List<MenuTreeVO> menuTree;
    private List<String> permissions;
    private DataScopeInfo dataScope;

    public List<MenuTreeVO> getMenuTree() { return menuTree; }
    public void setMenuTree(List<MenuTreeVO> menuTree) { this.menuTree = menuTree; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public DataScopeInfo getDataScope() { return dataScope; }
    public void setDataScope(DataScopeInfo dataScope) { this.dataScope = dataScope; }

    public static class DataScopeInfo {
        private String scopeType;
        private Long userOrgId;
        private List<Long> deptOrgIds;

        public String getScopeType() { return scopeType; }
        public void setScopeType(String scopeType) { this.scopeType = scopeType; }
        public Long getUserOrgId() { return userOrgId; }
        public void setUserOrgId(Long userOrgId) { this.userOrgId = userOrgId; }
        public List<Long> getDeptOrgIds() { return deptOrgIds; }
        public void setDeptOrgIds(List<Long> deptOrgIds) { this.deptOrgIds = deptOrgIds; }
    }
}
