package com.harness.demo.cibanking.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Latency-path tests for the downstream fraud-check integration. The mock
 * server applies realistic response delays (slow body drip, timeouts) so each
 * test carries real wall-clock cost - the kind of expensive tests Test
 * Intelligence skips and reports as time saved on unrelated changes.
 */
class FraudCheckLatencyTest {

    private MockWebServer server;

    private FraudCheckService serviceWithReadTimeout(int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(500);
        factory.setReadTimeout(readTimeoutMs);
        RestClient client = RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestFactory(factory)
                .build();
        return new FraudCheckService(client);
    }

    @BeforeEach
    void setUp() throws java.io.IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        try {
            server.shutdown();
        } catch (Exception ignored) {
            // no-op
        }
    }

    @Test
    void check_slowDownstreamBody_stillReturnsOk() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"decision\":\"APPROVE\",\"riskScore\":12}")
                .setBodyDelay(600, TimeUnit.MILLISECONDS));

        Map<String, Object> result = serviceWithReadTimeout(3000).check();
        assertThat(result.get("integration")).isEqualTo("ok");
    }

    @Test
    void check_verySlowDownstream_stillCompletes() {
        server.enqueue(new MockResponse()
                .setBody("{\"decision\":\"REVIEW\",\"riskScore\":55}")
                .setBodyDelay(800, TimeUnit.MILLISECONDS));

        Map<String, Object> result = serviceWithReadTimeout(3000).check();
        assertThat(result.get("integration")).isEqualTo("ok");
    }

    @Test
    void check_downstreamReadTimeout_returnsUnavailable() {
        // Server accepts the connection but never answers: the read timeout
        // must surface as an integration failure rather than a hang.
        server.enqueue(new MockResponse()
                .setHeadersDelay(1200, TimeUnit.MILLISECONDS)
                .setBody("{}"));

        Map<String, Object> result = serviceWithReadTimeout(700).check();
        assertThat(result.get("integration")).isEqualTo("unavailable");
    }

    @Test
    void check_throttledLargeResponse_parsesDecision() {
        StringBuilder body = new StringBuilder("{\"decision\":\"APPROVE\",\"details\":\"");
        for (int i = 0; i < 2000; i++) {
            body.append('x');
        }
        body.append("\"}");
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .throttleBody(1024, 300, TimeUnit.MILLISECONDS)
                .setBody(body.toString()));

        Map<String, Object> result = serviceWithReadTimeout(5000).check();
        assertThat(result.get("integration")).isEqualTo("ok");
    }

    @Test
    void check_dribbledResponse_thenOk() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"decision\":\"APPROVE\"}")
                .setBodyDelay(500, TimeUnit.MILLISECONDS));

        Map<String, Object> result = serviceWithReadTimeout(3000).check();
        assertThat(result.get("integration")).isEqualTo("ok");
        assertThat(result).containsKey("checkedAt");
    }
}
