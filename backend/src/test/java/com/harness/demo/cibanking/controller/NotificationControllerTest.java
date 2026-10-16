package com.harness.demo.cibanking.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "cibanking.warmup.enabled=false")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void getNotifications_returnsAllNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id").value("ntf-001"));
    }

    @Test
    @Order(2)
    void getUnreadCount_returnsTwoBeforeAnyReads() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread").value(2));
    }

    @Test
    @Order(3)
    void markRead_marksNotificationRead() throws Exception {
        mockMvc.perform(post("/api/notifications/ntf-001/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ntf-001"))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @Order(4)
    void getUnreadCount_dropsAfterMarkRead() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread").value(1));
    }

    @Test
    @Order(5)
    void markRead_returns404ForUnknownId() throws Exception {
        mockMvc.perform(post("/api/notifications/nope/read"))
                .andExpect(status().isNotFound());
    }
}
