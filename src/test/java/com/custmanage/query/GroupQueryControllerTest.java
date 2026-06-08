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
class GroupQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void lists_groups_with_basic_income_and_owner_fields() throws Exception {
        mockMvc.perform(get("/api/groups")
                .param("groupName", "某某")
                .param("pageNum", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records", hasSize(1)))
            .andExpect(jsonPath("$.data.records[0].groupId").value(1000))
            .andExpect(jsonPath("$.data.records[0].groupCode").value("GRP001"))
            .andExpect(jsonPath("$.data.records[0].groupName").value("苏州某某集团股份有限公司"))
            .andExpect(jsonPath("$.data.records[0].buName").value("交通能源BU"))
            .andExpect(jsonPath("$.data.records[0].managerName").value("张三"))
            .andExpect(jsonPath("$.data.records[0].lastMonthRevenue").value(15000.00));
    }

    @Test
    void returns_group_detail_by_id() throws Exception {
        mockMvc.perform(get("/api/groups/1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.groupCode").value("GRP001"))
            .andExpect(jsonPath("$.data.groupName").value("苏州某某集团股份有限公司"))
            .andExpect(jsonPath("$.data.buName").value("交通能源BU"))
            .andExpect(jsonPath("$.data.managerName").value("张三"))
            .andExpect(jsonPath("$.data.important").value(true))
            .andExpect(jsonPath("$.data.keyCustomer").value(true))
            .andExpect(jsonPath("$.data.mainContactPhone").value("13812345678"));
    }

    @Test
    void returns_not_found_when_group_does_not_exist() throws Exception {
        mockMvc.perform(get("/api/groups/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }
}
