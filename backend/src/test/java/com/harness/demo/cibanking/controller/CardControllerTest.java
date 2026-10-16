package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "cibanking.warmup.enabled=false")
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCards_returnsAllCards() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void getCards_filtersByAccountId() throws Exception {
        mockMvc.perform(get("/api/cards").param("accountId", "acc-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].accountId", everyItem(is("acc-001"))));
    }

    @Test
    void getCard_returnsSingleCard() throws Exception {
        mockMvc.perform(get("/api/cards/card-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.network").value("Visa"))
                .andExpect(jsonPath("$.type").value("DEBIT"));
    }

    @Test
    void getCard_returns404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/cards/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleFreeze_flipsStatusEachCall() throws Exception {
        MvcResult before = mockMvc.perform(get("/api/cards/card-001"))
                .andExpect(status().isOk())
                .andReturn();
        String original = com.jayway.jsonpath.JsonPath
                .read(before.getResponse().getContentAsString(), "$.status");
        String flipped = "ACTIVE".equals(original) ? "FROZEN" : "ACTIVE";

        mockMvc.perform(post("/api/cards/card-001/freeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(flipped));
        mockMvc.perform(post("/api/cards/card-001/freeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(original));
    }

    @Test
    void toggleFreeze_returns404ForUnknownId() throws Exception {
        mockMvc.perform(post("/api/cards/nope/freeze"))
                .andExpect(status().isNotFound());
    }
}
