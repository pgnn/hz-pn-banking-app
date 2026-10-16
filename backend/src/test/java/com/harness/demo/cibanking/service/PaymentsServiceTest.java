package com.harness.demo.cibanking.service;

import com.harness.demo.cibanking.model.Payee;
import com.harness.demo.cibanking.model.ScheduledPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the in-memory payees / scheduled payments and the transfer
 * delegation into {@link BankingService}.
 */
class PaymentsServiceTest {

    private PaymentsService service;

    @BeforeEach
    void setUp() {
        service = new PaymentsService(new BankingService());
    }

    @Test
    void getPayees_returnsAllSeededPayees() {
        List<Payee> payees = service.getPayees();
        assertThat(payees).hasSize(5);
        assertThat(payees).extracting(Payee::getName)
                .containsExactly("Oakwood Lettings", "British Gas", "J Patterson", "HMRC", "Sunrise Nursery");
    }

    @Test
    void getPayees_payeeWithoutPayment_hasNullLastPaidFields() {
        Payee nursery = service.getPayees().stream()
                .filter(p -> "payee-005".equals(p.getId())).findFirst().orElseThrow();
        assertThat(nursery.getLastPaidDate()).isNull();
        assertThat(nursery.getLastAmount()).isNull();
    }

    @Test
    void getScheduledPayments_returnsAllSeededEntries() {
        assertThat(service.getScheduledPayments()).hasSize(7);
    }

    @Test
    void getDirectDebits_returnsOnlyDirectDebitKind() {
        List<ScheduledPayment> debits = service.getDirectDebits();
        assertThat(debits).hasSize(4);
        assertThat(debits).allSatisfy(d -> assertThat(d.getKind()).isEqualTo("DIRECT_DEBIT"));
    }

    @Test
    void getStandingOrders_returnsOnlyStandingOrderKind() {
        List<ScheduledPayment> orders = service.getStandingOrders();
        assertThat(orders).hasSize(3);
        assertThat(orders).allSatisfy(o -> assertThat(o.getKind()).isEqualTo("STANDING_ORDER"));
    }

    @Test
    void getScheduledPayments_includesPausedEntries() {
        assertThat(service.getScheduledPayments())
                .anySatisfy(s -> {
                    assertThat(s.getId()).isEqualTo("sp-005");
                    assertThat(s.getStatus()).isEqualTo("PAUSED");
                });
    }

    @Test
    void transfer_movesMoneyBetweenAccounts() {
        Map<String, Object> result = service.transfer("acc-001", "acc-002",
                new BigDecimal("50.00"), "Save");

        assertThat(result.get("status")).isEqualTo("COMPLETED");
        assertThat(result.get("reference")).isEqualTo("Save");
        assertThat((BigDecimal) result.get("fromBalance")).isEqualByComparingTo("3195.67");
        assertThat((BigDecimal) result.get("toBalance")).isEqualByComparingTo("12530.00");
    }

    @Test
    void transfer_propagatesBankingServiceValidationErrors() {
        assertThatThrownBy(() -> service.transfer("acc-001", "acc-001",
                new BigDecimal("10.00"), "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same account");
    }
}
