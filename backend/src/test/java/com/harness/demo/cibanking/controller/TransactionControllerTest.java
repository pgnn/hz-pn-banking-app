package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "cibanking.warmup.enabled=false")
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void search_filtersByAccountId() throws Exception {
        mockMvc.perform(get("/api/transactions").param("accountId", "acc-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$[*].accountId", everyItem(is("acc-003"))));
    }

    @Test
    void search_filtersByQuery() throws Exception {
        mockMvc.perform(get("/api/transactions").param("query", "salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void search_filtersByType() throws Exception {
        mockMvc.perform(get("/api/transactions").param("type", "CREDIT")
                        .param("accountId", "acc-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].type", everyItem(is("CREDIT"))));
    }

    @Test
    void search_filtersByDateRange() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("accountId", "acc-001")
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)));
    }

    @Test
    void search_returnsEmptyWhenNothingMatches() throws Exception {
        mockMvc.perform(get("/api/transactions").param("query", "zzz-no-match"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void categories_returnsSortedDistinctList() throws Exception {
        mockMvc.perform(get("/api/transactions/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)))
                .andExpect(jsonPath("$[0]").value("Bills & utilities"));
    }
}
