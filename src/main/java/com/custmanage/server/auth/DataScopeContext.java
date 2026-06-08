package com.custmanage.server.auth;

import java.util.Collections;
import java.util.List;

/**
 * 解析后的数据权限范围。
 */
public class DataScopeContext {

    private String scopeType;
    private Long currentUserId;
    private Long userOrgId;
    /** 本部门时：该部门下所有网格的 org_id 列表 */
    private List<Long> deptOrgIds;

    public DataScopeContext() {
        this.deptOrgIds = Collections.emptyList();
    }

    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }

    public Long getCurrentUserId() { return currentUserId; }
    public void setCurrentUserId(Long currentUserId) { this.currentUserId = currentUserId; }

    public Long getUserOrgId() { return userOrgId; }
    public void setUserOrgId(Long userOrgId) { this.userOrgId = userOrgId; }

    public List<Long> getDeptOrgIds() { return deptOrgIds; }
    public void setDeptOrgIds(List<Long> deptOrgIds) { this.deptOrgIds = deptOrgIds; }
}
