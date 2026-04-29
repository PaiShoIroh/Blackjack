package com.aditya.blackjack.domain.hand;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import com.aditya.blackjack.domain.card.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HandTest {
    private Hand hand;

    @BeforeEach
    void setUp() {
        hand = new Hand();
    }

    //

    @Test
    void simpleHandValueIsSumOfCards() {
        hand.addCard(new Card(Rank.SEVEN, Suit.DIAMONDS));
        hand.addCard(new Card(Rank.EIGHT, Suit.DIAMONDS));
        assertThat(hand.getValue()).isEqualTo(15);
    }

    @Test
    void aceCountsAsElevenWhenSoft() {
        hand.addCard(new Card(Rank.ACE, Suit.DIAMONDS));
        hand.addCard(new Card(Rank.EIGHT, Suit.CLUBS));
        assertThat(hand.getValue()).isEqualTo(19);
    }

    @Test
    void aceReducesToOneWhenBustWithEleven() {
        hand.addCard(new Card(Rank.ACE, Suit.DIAMONDS));
        hand.addCard(new Card(Rank.SEVEN, Suit.CLUBS));
        hand.addCard(new Card(Rank.EIGHT, Suit.CLUBS));
        assertThat(hand.getValue()).isEqualTo(16); // not 26
    }

    @Test
    void twoAcesReducesOneToAvoidBust() {
        hand.addCard(new Card(Rank.ACE, Suit.DIAMONDS));
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));
        assertThat(hand.getValue()).isEqualTo(12); // 11 and 1
    }

    @Test
    void allAcesReduceIfNeeded() {
        hand.addCard(new Card(Rank.ACE, Suit.SPADES));
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.ACE, Suit.DIAMONDS));
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));
        assertThat(hand.getValue()).isEqualTo(14); // 11+1+1+1
    }

    // --- soft/hard ---

    @Test
    void handWithActiveAceIsSoft() {
        hand.addCard(new Card(Rank.ACE, Suit.SPADES));
        hand.addCard(new Card(Rank.SIX, Suit.HEARTS));
        assertThat(hand.isSoft()).isTrue();
    }

    @Test
    void handIsHardWhenAceReducedToOne() {
        hand.addCard(new Card(Rank.ACE, Suit.SPADES));
        hand.addCard(new Card(Rank.SEVEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.EIGHT, Suit.CLUBS));
        assertThat(hand.isSoft()).isFalse();
    }

    // --- bust ---

    @Test
    void handIsBustOver21() {
        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.EIGHT, Suit.CLUBS));
        hand.addCard(new Card(Rank.FIVE, Suit.DIAMONDS));
        assertThat(hand.isBust()).isTrue();
    }

    @Test
    void handIsNotBustAt21() {
        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.FIVE, Suit.CLUBS));
        hand.addCard(new Card(Rank.SIX, Suit.DIAMONDS));
        assertThat(hand.isBust()).isFalse();
    }

    // --- blackjack ---

    @Test
    void aceAndTenValueIsBlackjack() {
        hand.addCard(new Card(Rank.ACE, Suit.SPADES));
        hand.addCard(new Card(Rank.KING, Suit.HEARTS));
        assertThat(hand.isBlackjack()).isTrue();
    }

    @Test
    void twentyOneWithThreeCardsIsNotBlackjack() {
        hand.addCard(new Card(Rank.SEVEN, Suit.SPADES));
        hand.addCard(new Card(Rank.SEVEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.SEVEN, Suit.CLUBS));
        assertThat(hand.isBlackjack()).isFalse();
    }

    // --- split ---

    @Test
    void sameRankValueCanSplit() {
        hand.addCard(new Card(Rank.KING, Suit.HEARTS));
        hand.addCard(new Card(Rank.JACK, Suit.SPADES));
        assertThat(hand.canSplit()).isTrue(); // both worth 10
    }

    @Test
    void differentRankValueCannotSplit() {
        hand.addCard(new Card(Rank.KING, Suit.HEARTS));
        hand.addCard(new Card(Rank.NINE, Suit.SPADES));
        assertThat(hand.canSplit()).isFalse();
    }

    // --- double down ---

    @Test
    void canDoubleDownOnTwoCards() {
        hand.addCard(new Card(Rank.FIVE, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));
        assertThat(hand.canDoubleDown()).isTrue();
    }

    @Test
    void cannotDoubleDownAfterHit() {
        hand.addCard(new Card(Rank.FIVE, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));
        hand.addCard(new Card(Rank.TWO, Suit.DIAMONDS));
        assertThat(hand.canDoubleDown()).isFalse();
    }

    // --- status ---

    @Test
    void defaultStatusIsActive() {
        assertThat(hand.getStatus()).isEqualTo(HandStatus.ACTIVE);
    }

    @Test
    void statusCanBeUpdated() {
        hand.setStatus(HandStatus.STOOD);
        assertThat(hand.getStatus()).isEqualTo(HandStatus.STOOD);
    }
}