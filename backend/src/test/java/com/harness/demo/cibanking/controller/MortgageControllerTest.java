package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "cibanking.warmup.enabled=false")
@AutoConfigureMockMvc
class MortgageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMortgages_returnsAllMortgages() throws Exception {
        mockMvc.perform(get("/api/mortgages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("mtg-001"))
                .andExpect(jsonPath("$[0].product").value("2 Year Fixed 4.29%"));
    }

    @Test
    void getMortgage_returnsSingleMortgage() throws Exception {
        mockMvc.perform(get("/api/mortgages/mtg-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("mtg-002"))
                .andExpect(jsonPath("$.rateType").value("FIXED"))
                .andExpect(jsonPath("$.monthlyPayment").value(742.10));
    }

    @Test
    void getMortgage_returns404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/mortgages/nope"))
                .andExpect(status().isNotFound());
    }
}
