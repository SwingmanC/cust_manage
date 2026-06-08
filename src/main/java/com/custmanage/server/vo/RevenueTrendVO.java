package com.custmanage.server.vo;

import java.math.BigDecimal;

public class RevenueTrendVO {
    private String revenueMonth;
    private BigDecimal revenueAmount;

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
}
