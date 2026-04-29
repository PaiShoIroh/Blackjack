package com.aditya.blackjack.domain.table;

import lombok.Getter;

@Getter
public class TableConfig {
    private final int numberOfDecks;     // typically 6 or 8
    private final int numberOfSeats;     // max seats at table
    private final int minimumBet;
    private final int maximumBet;
    private final boolean hitOnSoft17;   // dealer rule

    public TableConfig(int numberOfDecks, int numberOfSeats,
                       int minimumBet, int maximumBet, boolean hitOnSoft17) {
        this.numberOfSeats = 0;
        this.numberOfDecks = 0;
        this.minimumBet = 0;
        this.maximumBet = 0;
        this.hitOnSoft17 = false;
    }

    // getters only — config is immutable
    public int getNumberOfDecks() { return numberOfDecks; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public int getMinimumBet() { return minimumBet; }
    public int getMaximumBet() { return maximumBet; }
    public boolean isHitOnSoft17() { return hitOnSoft17; }
}
