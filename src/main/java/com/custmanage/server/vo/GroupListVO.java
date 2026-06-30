package com.custmanage.server.vo;

import java.math.BigDecimal;

public class GroupListVO {
    private Long groupId;
    private Long groupCode;
    private String groupName;
    private String groupCategory;
    private String groupTypeCode;
    private String buName;
    private String managerName;
    private Boolean important;
    private Boolean keyCustomer;
    private Boolean is139;
    private String locationType;
    private String contactBookStatus;
    private BigDecimal lastMonthRevenue;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(Long groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCategory() {
        return groupCategory;
    }

    public void setGroupCategory(String groupCategory) {
        this.groupCategory = groupCategory;
    }

    public String getGroupTypeCode() {
        return groupTypeCode;
    }

    public void setGroupTypeCode(String groupTypeCode) {
        this.groupTypeCode = groupTypeCode;
    }

    public String getBuName() {
        return buName;
    }

    public void setBuName(String buName) {
        this.buName = buName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
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

    public String getContactBookStatus() {
        return contactBookStatus;
    }

    public void setContactBookStatus(String contactBookStatus) {
        this.contactBookStatus = contactBookStatus;
    }

    public BigDecimal getLastMonthRevenue() {
        return lastMonthRevenue;
    }

    public void setLastMonthRevenue(BigDecimal lastMonthRevenue) {
        this.lastMonthRevenue = lastMonthRevenue;
    }
}
