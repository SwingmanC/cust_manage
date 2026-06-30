package com.custmanage.server.dto;

public class BcQueryRequest {
    private String statMonth;
    private Long serviceId;
    /** 业务科目名称筛选（模糊匹配 service_name） */
    private String serviceName;

    public String getStatMonth() {
        return statMonth;
    }

    public void setStatMonth(String statMonth) {
        this.statMonth = statMonth;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
