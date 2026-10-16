package com.harness.demo.cibanking.service;

import com.harness.demo.cibanking.model.Account;
import com.harness.demo.cibanking.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the query/aggregation side of {@link BankingService}:
 * transaction search, categories, insights, statements, nicknames and summary.
 */
class BankingServiceQueryTest {

    private BankingService service;

    @BeforeEach
    void setUp() {
        service = new BankingService();
    }

    @Test
    void searchTransactions_withNoFilters_returnsEverythingMostRecentFirst() {
        List<Transaction> all = service.searchTransactions(null, null, null, null, null, null);
        assertThat(all).hasSize(33);
        assertThat(all.get(0).getDate())
                .isGreaterThanOrEqualTo(all.get(all.size() - 1).getDate());
    }

    @Test
    void searchTransactions_filtersByQueryAcrossDescriptionAndCategory() {
        List<Transaction> results = service.searchTransactions("salary", null, null, null, null, null);
        assertThat(results).hasSize(5);
        assertThat(results).allSatisfy(t -> assertThat(
                t.getDescription().toLowerCase() + t.getCategory().toLowerCase()).contains("salary"));
    }

    @Test
    void searchTransactions_filtersByCategoryCaseInsensitively() {
        List<Transaction> results = service.searchTransactions(null, "groceries", null, null, null, null);
        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(t -> assertThat(t.getCategory()).isEqualTo("Groceries"));
    }

    @Test
    void searchTransactions_filtersByType() {
        List<Transaction> credits = service.searchTransactions(null, null, "CREDIT", null, null, null);
        assertThat(credits).isNotEmpty();
        assertThat(credits).allSatisfy(t -> assertThat(t.getAmount()).isPositive());
    }

    @Test
    void searchTransactions_filtersByAccountId() {
        List<Transaction> results = service.searchTransactions(null, null, null, "acc-002", null, null);
        assertThat(results).hasSize(5);
        assertThat(results).allSatisfy(t -> assertThat(t.getAccountId()).isEqualTo("acc-002"));
    }

    @Test
    void searchTransactions_filtersByDateRange() {
        List<Transaction> results = service.searchTransactions(
                null, null, null, "acc-001", "2026-07-01", "2026-07-31");
        assertThat(results).hasSize(12);
        assertThat(results).allSatisfy(t ->
                assertThat(t.getDate()).isBetween("2026-07-01", "2026-07-31"));
    }

    @Test
    void searchTransactions_combinesFilters() {
        List<Transaction> results = service.searchTransactions(
                null, null, "DEBIT", "acc-001", "2026-06-01", "2026-06-30");
        assertThat(results).hasSize(3);
        assertThat(results).allSatisfy(t -> {
            assertThat(t.getAccountId()).isEqualTo("acc-001");
            assertThat(t.getAmount()).isNegative();
        });
    }

    @Test
    void getCategories_returnsDistinctSortedNonBlankCategories() {
        List<String> categories = service.getCategories();
        assertThat(categories).hasSize(15);
        assertThat(categories).isSorted();
        assertThat(categories).doesNotContainNull().allSatisfy(c -> assertThat(c).isNotBlank());
        assertThat(categories).contains("Income", "Eating out", "Software & cloud");
    }

    @Test
    void getInsights_returnsInOutNetAndCategoryBreakdown() {
        Map<String, Object> insights = service.getInsights("acc-001");
        assertThat(insights).isNotNull();
        assertThat(insights.get("accountId")).isEqualTo("acc-001");
        assertThat(insights.get("transactionCount")).isEqualTo(18);
        assertThat((BigDecimal) insights.get("moneyIn")).isEqualByComparingTo("7950.00");

        BigDecimal moneyIn = (BigDecimal) insights.get("moneyIn");
        BigDecimal moneyOut = (BigDecimal) insights.get("moneyOut");
        BigDecimal net = (BigDecimal) insights.get("net");
        assertThat(net).isEqualByComparingTo(moneyIn.subtract(moneyOut));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) insights.get("categories");
        assertThat(categories).isNotEmpty();
        assertThat(categories.get(0).get("category")).isEqualTo("Housing");
    }

    @Test
    void getInsights_returnsNullForUnknownAccount() {
        assertThat(service.getInsights("nope")).isNull();
    }

    @Test
    void getStatements_groupsTransactionsByMonthMostRecentFirst() {
        List<Map<String, Object>> statements = service.getStatements("acc-001");
        assertThat(statements).hasSize(3);
        assertThat(statements.get(0).get("period")).isEqualTo("2026-07");
        assertThat(statements.get(0).get("transactionCount")).isEqualTo(12);
        assertThat(statements.get(2).get("period")).isEqualTo("2026-05");
    }

    @Test
    void getStatements_returnsNullForUnknownAccount() {
        assertThat(service.getStatements("nope")).isNull();
    }

    @Test
    void updateNickname_renamesAccount() {
        Account updated = service.updateNickname("acc-002", "Holiday Fund");
        assertThat(updated).isNotNull();
        assertThat(updated.getNickname()).isEqualTo("Holiday Fund");
        assertThat(service.getAccount("acc-002").getNickname()).isEqualTo("Holiday Fund");
    }

    @Test
    void updateNickname_returnsNullForUnknownAccount() {
        assertThat(service.updateNickname("nope", "x")).isNull();
    }

    @Test
    void getSummary_aggregatesAccountsAndMortgages() {
        Map<String, Object> summary = service.getSummary();
        assertThat(summary.get("accountCount")).isEqualTo(3);
        assertThat(summary.get("mortgageCount")).isEqualTo(2);
        assertThat((BigDecimal) summary.get("totalBalance")).isEqualByComparingTo("73936.09");
        assertThat((BigDecimal) summary.get("totalMortgageOutstanding")).isEqualByComparingTo("356500.00");
        assertThat(summary.get("statusMessage")).isEqualTo("Portfolio: 3 accounts, 2 mortgages");
    }
}
