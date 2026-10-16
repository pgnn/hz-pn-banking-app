package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for GET /api/accounts/{id}/statements. Distinct context
 * property = its own Spring context (heavier; skippable by TI on unrelated PRs).
 */
@SpringBootTest(properties = {
        "cibanking.warmup.enabled=false",
        "cibanking.test.context=statements"
})
@AutoConfigureMockMvc
class AccountStatementsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStatements_returnsMonthlyBucketsMostRecentFirst() throws Exception {
        mockMvc.perform(get("/api/accounts/acc-001/statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].period").value("2026-07"))
                .andExpect(jsonPath("$[0].transactionCount").value(12))
                .andExpect(jsonPath("$[2].period").value("2026-05"));
    }

    @Test
    void getStatements_returns404ForUnknownAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/nope/statements"))
                .andExpect(status().isNotFound());
    }
}
