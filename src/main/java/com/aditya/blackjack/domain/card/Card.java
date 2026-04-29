package com.aditya.blackjack.domain.card;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Card {
    private final Rank rank;
    private final Suit suit;

    @Override
    public String toString() { return rank + " of " + suit; }
}
