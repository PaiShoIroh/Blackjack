package com.aditya.blackjack.domain.dealer;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import com.aditya.blackjack.domain.card.Suit;
import com.aditya.blackjack.domain.hand.HandStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DealerTest {

    @Test
    void hitsOnSixteen() {
        Dealer dealer = new Dealer(false);
        dealer.receiveCard(new Card(Rank.EIGHT, Suit.CLUBS));
        dealer.receiveCard(new Card(Rank.EIGHT, Suit.HEARTS));
        assertThat(dealer.shouldHit()).isTrue();
    }

    @Test
    void standsOnHardSeventeenWithoutAce() {
        Dealer dealer = new Dealer(false);
        dealer.receiveCard(new Card(Rank.KING, Suit.CLUBS));
        dealer.receiveCard(new Card(Rank.SEVEN, Suit.DIAMONDS));
        assertThat(dealer.shouldHit()).isFalse();
    }

    @Test
    void standsOnHardSeventeenWithAce() {
        Dealer dealer = new Dealer(false);
        dealer.receiveCard(new Card(Rank.ACE, Suit.CLUBS));
        dealer.receiveCard(new Card(Rank.SIX, Suit.DIAMONDS));
        dealer.receiveCard(new Card(Rank.TEN, Suit.DIAMONDS));
        assertThat(dealer.shouldHit()).isFalse();
    }

    @Test
    void standsOnSoftSeventeenWhenRuleDisabled() {
        Dealer dealer = new Dealer(false);
        dealer.receiveCard(new Card(Rank.ACE, Suit.SPADES));
        dealer.receiveCard(new Card(Rank.SIX, Suit.HEARTS));
        assertThat(dealer.shouldHit()).isFalse();
    }

    @Test
    void hitsOnSoftSeventeenWhenRuleEnabled() {
        Dealer dealer = new Dealer(true);
        dealer.receiveCard(new Card(Rank.ACE, Suit.SPADES));
        dealer.receiveCard(new Card(Rank.SIX, Suit.HEARTS));
        assertThat(dealer.shouldHit()).isTrue();
    }

    @Test
    void standsOnEighteen() {
        Dealer dealer = new Dealer(true);
        dealer.receiveCard(new Card(Rank.TEN, Suit.HEARTS));
        dealer.receiveCard(new Card(Rank.EIGHT, Suit.CLUBS));
        assertThat(dealer.shouldHit()).isFalse();
    }

    @Test
    void hitsOnSoftEighteenIsNotTriggered() {
        // soft 18 — dealer always stands regardless of soft-17 rule
        Dealer dealer = new Dealer(true);
        dealer.receiveCard(new Card(Rank.ACE, Suit.SPADES));
        dealer.receiveCard(new Card(Rank.SEVEN, Suit.HEARTS));
        assertThat(dealer.shouldHit()).isFalse();
    }

    // --- reset ---

    @Test
    void resetClearsHand() {
        Dealer dealer = new Dealer(false);
        dealer.receiveCard(new Card(Rank.TEN, Suit.HEARTS));
        dealer.receiveCard(new Card(Rank.SIX, Suit.CLUBS));
        dealer.reset();
        assertThat(dealer.getHand().getCards()).isEmpty();
    }

    @Test
    void resetHandIsActive() {
        Dealer dealer = new Dealer(false);
        dealer.receiveCard(new Card(Rank.TEN, Suit.HEARTS));
        dealer.reset();
        assertThat(dealer.getHand().getStatus()).isEqualTo(HandStatus.ACTIVE);
    }
}
