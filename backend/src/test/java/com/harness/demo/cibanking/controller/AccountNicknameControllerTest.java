package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for PUT /api/accounts/{id}/nickname. Distinct context
 * property = its own Spring context (heavier; skippable by TI on unrelated PRs).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "cibanking.warmup.enabled=false",
        "cibanking.test.context=nickname"
})
@AutoConfigureMockMvc
class AccountNicknameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void updateNickname_renamesAccount() throws Exception {
        mockMvc.perform(put("/api/accounts/acc-003/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"Treasury Ops Account\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("acc-003"))
                .andExpect(jsonPath("$.nickname").value("Treasury Ops Account"));
    }

    @Test
    void updateNickname_returns400ForBlankNickname() throws Exception {
        mockMvc.perform(put("/api/accounts/acc-001/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNickname_returns404ForUnknownAccount() throws Exception {
        mockMvc.perform(put("/api/accounts/nope/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
}
