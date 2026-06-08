package com.custmanage.query;

import com.custmanage.server.CustManageServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CustManageServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupDetailDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returns_revenue_monthly_rows_for_group() throws Exception {
        mockMvc.perform(get("/api/groups/1000/revenues")
                .param("startMonth", "2026-01")
                .param("endMonth", "2026-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].serviceName").value("移动云"))
            .andExpect(jsonPath("$.data[0].revenueMonth").value("2026-01"))
            .andExpect(jsonPath("$.data[0].revenueAmount").value(12000.50))
            .andExpect(jsonPath("$.data[1].serviceName").value("专线"))
            .andExpect(jsonPath("$.data[1].revenueAmount").value(8000.00));
    }

    @Test
    void returns_revenue_trend_by_month() throws Exception {
        mockMvc.perform(get("/api/groups/1000/revenues/trend")
                .param("startMonth", "2026-01")
                .param("endMonth", "2026-02"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].revenueMonth").value("2026-01"))
            .andExpect(jsonPath("$.data[0].revenueAmount").value(20000.50))
            .andExpect(jsonPath("$.data[1].revenueMonth").value("2026-02"))
            .andExpect(jsonPath("$.data[1].revenueAmount").value(15000.00));
    }

    @Test
    void returns_bc_rows_for_group() throws Exception {
        mockMvc.perform(get("/api/groups/1000/bc")
                .param("statMonth", "2026-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].serviceName").value("移动云"))
            .andExpect(jsonPath("$.data[0].quantity").value(8))
            .andExpect(jsonPath("$.data[1].serviceName").value("专线"))
            .andExpect(jsonPath("$.data[1].quantity").value(12));
    }

    @Test
    void returns_competitor_rows_for_group() throws Exception {
        mockMvc.perform(get("/api/groups/1000/competitors")
                .param("statMonth", "2026-01")
                .param("competitorName", "电信"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].serviceName").value("专线"))
            .andExpect(jsonPath("$.data[0].competitorName").value("电信"))
            .andExpect(jsonPath("$.data[0].quantity").value(3))
            .andExpect(jsonPath("$.data[0].remark").value("集团有存量专线"));
    }
}
