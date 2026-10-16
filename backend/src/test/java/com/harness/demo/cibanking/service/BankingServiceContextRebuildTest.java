package com.harness.demo.cibanking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring-context lifecycle tests. {@link DirtiesContext} in
 * BEFORE_EACH_TEST_METHOD mode forces a full application-context rebuild before
 * every test method, so each test carries real context-startup cost. These are
 * exactly the kind of expensive tests Test Intelligence skips (and shows as
 * time saved) on PRs that do not touch banking logic.
 */
@SpringBootTest(properties = {
        "cibanking.warmup.enabled=false",
        "cibanking.test.context=context-rebuild"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BankingServiceContextRebuildTest {

    @Autowired
    private BankingService service;

    @Test
    void context_bootsWithAllSeededAccounts() {
        assertThat(service.getAccounts()).hasSize(3);
    }

    @Test
    void context_bootsWithAllSeededMortgages() {
        assertThat(service.getMortgages()).hasSize(2);
    }

    @Test
    void context_bootsWithAllSeededTransactions() {
        assertThat(service.getTransactions("acc-001")).hasSize(18);
        assertThat(service.getTransactions("acc-002")).hasSize(5);
        assertThat(service.getTransactions("acc-003")).hasSize(10);
    }

    @Test
    void context_summaryIsCorrectOnFreshBoot() {
        assertThat(service.getSummary().get("accountCount")).isEqualTo(3);
    }

    @Test
    void context_insightsIsCorrectOnFreshBoot() {
        assertThat(service.getInsights("acc-001").get("transactionCount")).isEqualTo(18);
    }
}
