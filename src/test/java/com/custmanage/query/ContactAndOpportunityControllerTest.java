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
class ContactAndOpportunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returns_all_contacts_for_group() throws Exception {
        mockMvc.perform(get("/api/groups/1000/contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(3)))
            .andExpect(jsonPath("$.data[0].contactName").value("王主任"))
            .andExpect(jsonPath("$.data[0].contactPhone").value("13812345678"))
            .andExpect(jsonPath("$.data[0].departmentName").value("信息技术部"));
    }

    @Test
    void returns_contacts_filtered_by_status() throws Exception {
        mockMvc.perform(get("/api/groups/1000/contacts")
                .param("status", "有效"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].status").value("有效"))
            .andExpect(jsonPath("$.data[1].status").value("有效"));
    }

    @Test
    void returns_all_opportunities_for_group() throws Exception {
        mockMvc.perform(get("/api/groups/1000/opportunities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(3)))
            .andExpect(jsonPath("$.data[0].opportunityName").value("5G专网部署项目"))
            .andExpect(jsonPath("$.data[0].opportunityStatus").value("跟进中"))
            .andExpect(jsonPath("$.data[0].expectedAmount").value(500000.00));
    }

    @Test
    void returns_opportunities_filtered_by_status() throws Exception {
        mockMvc.perform(get("/api/groups/1000/opportunities")
                .param("status", "成交"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].opportunityStatus").value("成交"));
    }

    @Test
    void returns_empty_contacts_for_group_with_none() throws Exception {
        mockMvc.perform(get("/api/groups/1001/contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
