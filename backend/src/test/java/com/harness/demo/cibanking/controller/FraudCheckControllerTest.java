package com.harness.demo.cibanking.controller;

import com.harness.demo.cibanking.service.FraudCheckService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the fraud-check controller's status mapping: 200 when the
 * downstream integration is healthy, 502 when it is unavailable.
 */
class FraudCheckControllerTest {

    @Test
    void check_returns200WhenIntegrationOk() {
        FraudCheckService svc = mock(FraudCheckService.class);
        when(svc.check()).thenReturn(Map.of("integration", "ok", "source", "downstream"));

        ResponseEntity<Map<String, Object>> res = new FraudCheckController(svc).check();

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("integration", "ok");
    }

    @Test
    void check_returns502WhenIntegrationUnavailable() {
        FraudCheckService svc = mock(FraudCheckService.class);
        when(svc.check()).thenReturn(Map.of("integration", "unavailable", "error", "ResourceAccessException"));

        ResponseEntity<Map<String, Object>> res = new FraudCheckController(svc).check();

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(res.getBody()).containsEntry("integration", "unavailable");
    }
}
