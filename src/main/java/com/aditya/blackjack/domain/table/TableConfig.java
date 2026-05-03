package com.aditya.blackjack.domain.table;

import lombok.Getter;

@Getter
public class TableConfig {

    private final int numberOfDecks;
    private final int numberOfSeats;
    private final int minimumBet;
    private final int maximumBet;
    private final boolean hitOnSoft17;

    public TableConfig(int numberOfDecks, int numberOfSeats,
                       int minimumBet, int maximumBet, boolean hitOnSoft17) {
        if (numberOfDecks <= 0) throw new IllegalArgumentException("Number of decks must be positive");
        if (numberOfSeats <= 0) throw new IllegalArgumentException("Number of seats must be positive");
        if (minimumBet <= 0) throw new IllegalArgumentException("Minimum bet must be positive");
        if (maximumBet < minimumBet) throw new IllegalArgumentException("Maximum bet cannot be less than minimum bet");
        this.numberOfDecks = numberOfDecks;
        this.numberOfSeats = numberOfSeats;
        this.minimumBet = minimumBet;
        this.maximumBet = maximumBet;
        this.hitOnSoft17 = hitOnSoft17;
    }
}