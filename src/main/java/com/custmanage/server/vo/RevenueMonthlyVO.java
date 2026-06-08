package com.custmanage.server.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RevenueMonthlyVO {
    private Long id;
    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private String revenueMonth;
    private BigDecimal revenueAmount;
    private String sourceSystem;
    private String syncBatchNo;
    private LocalDateTime syncTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRevenueMonth() {
        return revenueMonth;
    }

    public void setRevenueMonth(String revenueMonth) {
        this.revenueMonth = revenueMonth;
    }

    public BigDecimal getRevenueAmount() {
        return revenueAmount;
    }

    public void setRevenueAmount(BigDecimal revenueAmount) {
        this.revenueAmount = revenueAmount;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSyncBatchNo() {
        return syncBatchNo;
    }

    public void setSyncBatchNo(String syncBatchNo) {
        this.syncBatchNo = syncBatchNo;
    }

    public LocalDateTime getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(LocalDateTime syncTime) {
        this.syncTime = syncTime;
    }
}
