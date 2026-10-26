package com.harness.demo.cibanking.service;

import com.harness.demo.cibanking.model.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the in-memory card list and the freeze/unfreeze toggle.
 */
class CardServiceTest {

    private CardService service;

    @BeforeEach
    void setUp() {
        service = new CardService();
    }

    @Test
    void getCards_returnsAllSeededCards() {
        List<Card> cards = service.getCards();
        assertThat(cards).hasSize(4);
        assertThat(cards).extracting(Card::getId)
                .containsExactly("card-001", "card-002", "card-003", "card-004");
    }

    @Test
    void getCardsForAccount_returnsOnlyMatchingCards() {
        List<Card> cards = service.getCardsForAccount("acc-001");
        assertThat(cards).hasSize(2);
        assertThat(cards).extracting(Card::getId)
                .containsExactly("card-001", "card-003");
    }

    @Test
    void getCardsForAccount_returnsEmptyForUnknownAccount() {
        assertThat(service.getCardsForAccount("does-not-exist")).isEmpty();
    }

    @Test
    void getCardsForAccount_returnsEmptyForNullAccount() {
        assertThat(service.getCardsForAccount(null)).isEmpty();
    }

    @Test
    void getCard_returnsMatchingCard() {
        Card card = service.getCard("card-003");
        assertThat(card).isNotNull();
        assertThat(card.getType()).isEqualTo("CREDIT");
        assertThat(card.getNetwork()).isEqualTo("Mastercard");
    }

    @Test
    void getCard_returnsNullForUnknownId() {
        assertThat(service.getCard("nope")).isNull();
    }

    @Test
    void getCard_returnsNullForNullId() {
        assertThat(service.getCard(null)).isNull();
    }

    @Test
    void toggleFreeze_freezesAnActiveCard() {
        Card card = service.toggleFreeze("card-001");
        assertThat(card).isNotNull();
        assertThat(card.getStatus()).isEqualTo("FROZEN");
    }

    @Test
    void toggleFreeze_unfreezesAFrozenCard() {
        Card card = service.toggleFreeze("card-004");
        assertThat(card).isNotNull();
        assertThat(card.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void toggleFreeze_togglesBackAndForth() {
        service.toggleFreeze("card-001");
        Card card = service.toggleFreeze("card-001");
        assertThat(card.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void toggleFreeze_returnsNullForUnknownCard() {
        assertThat(service.toggleFreeze("nope")).isNull();
    }
}
