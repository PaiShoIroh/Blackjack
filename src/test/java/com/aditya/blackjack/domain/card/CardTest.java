package com.aditya.blackjack.domain.card;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CardTest {

    @Test
    void faceCardsAreWorthTen() {
        assertThat(new Card(Rank.JACK, Suit.HEARTS).getValue()).isEqualTo(10);
        assertThat(new Card(Rank.QUEEN, Suit.DIAMONDS).getValue()).isEqualTo(10);
        assertThat(new Card(Rank.KING, Suit.CLUBS).getValue()).isEqualTo(10);
    }

    @Test
    void aceIsWorthElevenByDefault() {
        assertThat(new Card(Rank.ACE, Suit.SPADES).getValue()).isEqualTo(11);
    }

    @Test
    void numberCardMatchesFaceValue() {
        assertThat(new Card(Rank.TWO, Suit.SPADES).getValue()).isEqualTo(2);
        assertThat(new Card(Rank.THREE, Suit.SPADES).getValue()).isEqualTo(3);
        assertThat(new Card(Rank.FOUR, Suit.SPADES).getValue()).isEqualTo(4);
        assertThat(new Card(Rank.FIVE, Suit.SPADES).getValue()).isEqualTo(5);
        assertThat(new Card(Rank.SIX, Suit.SPADES).getValue()).isEqualTo(6);
        assertThat(new Card(Rank.SEVEN, Suit.SPADES).getValue()).isEqualTo(7);
        assertThat(new Card(Rank.EIGHT, Suit.SPADES).getValue()).isEqualTo(8);
        assertThat(new Card(Rank.NINE, Suit.SPADES).getValue()).isEqualTo(9);
        assertThat(new Card(Rank.TEN, Suit.SPADES).getValue()).isEqualTo(10);
    }

    @Test
    void toStringIsHumanReadable() {
        Card card = new Card(Rank.ACE, Suit.SPADES);
        assertThat(card.toString()).isEqualTo("ACE of SPADES");
    }
}
