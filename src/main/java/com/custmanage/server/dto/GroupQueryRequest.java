package com.custmanage.server.dto;

import com.custmanage.server.auth.DataScopeContext;

public class GroupQueryRequest {
    private String groupName;
    private String groupCode;
    private String groupTypeCode;
    private Boolean important;
    private Boolean keyCustomer;
    private Boolean is139;
    private String locationType;
    private Long buOrgId;
    private Long managerUserId;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    /** 数据权限上下文，由后端 Service 层注入 */
    private DataScopeContext dataScope;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupTypeCode() {
        return groupTypeCode;
    }

    public void setGroupTypeCode(String groupTypeCode) {
        this.groupTypeCode = groupTypeCode;
    }

    public Boolean getImportant() {
        return important;
    }

    public void setImportant(Boolean important) {
        this.important = important;
    }

    public Boolean getKeyCustomer() {
        return keyCustomer;
    }

    public void setKeyCustomer(Boolean keyCustomer) {
        this.keyCustomer = keyCustomer;
    }

    public Boolean getIs139() {
        return is139;
    }

    public void setIs139(Boolean is139) {
        this.is139 = is139;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Long getBuOrgId() {
        return buOrgId;
    }

    public void setBuOrgId(Long buOrgId) {
        this.buOrgId = buOrgId;
    }

    public Long getManagerUserId() {
        return managerUserId;
    }

    public void setManagerUserId(Long managerUserId) {
        this.managerUserId = managerUserId;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public int offset() {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        return (safePageNum - 1) * safePageSize;
    }

    public DataScopeContext getDataScope() {
        return dataScope;
    }

    public void setDataScope(DataScopeContext dataScope) {
        this.dataScope = dataScope;
    }

    public int limit() {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
