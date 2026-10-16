package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for GET /api/accounts/{id}/insights. Uses a distinct context
 * property so this class boots its own Spring context - heavier, but isolated
 * from other suites, which is what Test Intelligence skips on unaffected PRs.
 */
@SpringBootTest(properties = {
        "cibanking.warmup.enabled=false",
        "cibanking.test.context=insights"
})
@AutoConfigureMockMvc
class AccountInsightsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getInsights_returnsInOutNetBreakdown() throws Exception {
        mockMvc.perform(get("/api/accounts/acc-001/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("acc-001"))
                .andExpect(jsonPath("$.transactionCount").value(greaterThanOrEqualTo(18)))
                .andExpect(jsonPath("$.moneyIn").value(7950.00))
                .andExpect(jsonPath("$.net").exists())
                .andExpect(jsonPath("$.categories", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.categories[0].category").value("Housing"));
    }

    @Test
    void getInsights_returns404ForUnknownAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/nope/insights"))
                .andExpect(status().isNotFound());
    }
}
