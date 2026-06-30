package com.custmanage.server.dto;

public class MonthRangeRequest {
    private String startMonth;
    private String endMonth;
    /** 业务科目名称筛选（模糊匹配 service_name） */
    private String serviceName;

    public String getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(String startMonth) {
        this.startMonth = startMonth;
    }

    public String getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(String endMonth) {
        this.endMonth = endMonth;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
