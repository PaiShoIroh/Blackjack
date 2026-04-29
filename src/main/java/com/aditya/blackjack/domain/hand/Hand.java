package com.aditya.blackjack.domain.hand;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Hand {

    private final List<Card> cards = new ArrayList<>();
    @Setter
    private HandStatus status = HandStatus.ACTIVE;

    public void addCard(Card card) {
        cards.add(card);
    }

    public int getValue() {
        int total = 0;
        int ace = 0;

        for (Card card : cards) {
            total += card.getValue();
            if (card.getRank() == Rank.ACE)
                ace++;
        }

        // reduce aces from 11 to 1 until we are not bust (or no aces are left)
        while (total > 21 && ace > 0) {
            total -= 10;
            ace--;
        }

        return total;
    }

    public boolean isSoft() {
        int total = 0;
        int aces = 0;

        for (Card card : cards) {
            total += card.getValue();
            if (card.getRank() == Rank.ACE)
                aces++;
        }

        // a hand is soft if an ace is still counting as 11
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return aces > 0;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }

    public boolean isBust() {
        return getValue() > 21;
    }

    public boolean canSplit() {
        // Always use value.
        // K and J can split as both are worth 10
        // If we don't want to allow such splits, we can set a table config
        return cards.size() == 2 &&
                cards.get(0).getRank().getValue() == cards.get(1).getRank().getValue();
    }

    public boolean canDoubleDown() {
        return cards.size() == 2;
    }
}
