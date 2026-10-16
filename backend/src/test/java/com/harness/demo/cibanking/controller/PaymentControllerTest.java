package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "cibanking.warmup.enabled=false")
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPayees_returnsAllPayees() throws Exception {
        mockMvc.perform(get("/api/payees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].name").value("Oakwood Lettings"));
    }

    @Test
    void getScheduled_returnsAllScheduledPayments() throws Exception {
        mockMvc.perform(get("/api/payments/scheduled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));
    }

    @Test
    void getDirectDebits_returnsOnlyDirectDebits() throws Exception {
        mockMvc.perform(get("/api/direct-debits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].kind", everyItem(is("DIRECT_DEBIT"))));
    }

    @Test
    void getStandingOrders_returnsOnlyStandingOrders() throws Exception {
        mockMvc.perform(get("/api/standing-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].kind", everyItem(is("STANDING_ORDER"))));
    }

    @Test
    void transfer_completesBetweenOwnAccounts() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":\"acc-001\",\"toAccountId\":\"acc-002\",\"amount\":25.00,\"reference\":\"Test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.reference").value("Test"))
                .andExpect(jsonPath("$.fromAccountId").value("acc-001"))
                .andExpect(jsonPath("$.toAccountId").value("acc-002"));
    }

    @Test
    void transfer_returns400WhenAmountMissing() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":\"acc-001\",\"toAccountId\":\"acc-002\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Amount is required"));
    }

    @Test
    void transfer_returns400WhenAmountNotNumeric() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":\"acc-001\",\"toAccountId\":\"acc-002\",\"amount\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Amount is not a valid number"));
    }

    @Test
    void transfer_returns400ForSameAccount() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":\"acc-001\",\"toAccountId\":\"acc-001\",\"amount\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot transfer to the same account"));
    }

    @Test
    void transfer_returns400ForInsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":\"acc-001\",\"toAccountId\":\"acc-002\",\"amount\":99999999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient available balance"));
    }
}
