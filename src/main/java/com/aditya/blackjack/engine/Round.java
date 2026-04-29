package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.table.Table;
import lombok.Getter;

import java.util.Map;

public class Round {
    private final Table table;
    @Getter
    private RoundPhase phase;
    @Getter
    private Map<Seat, RoundOutcome> outcomes;

    public Round(Table table) {
        this.table = null;
    }

    public void collectBets() { }
    public void deal() { }              // 2 cards each, seat order, dealer last
    public void playSeats() { }         // iterate occupied seats, handle actions
    public void playDealer() { }
    public void settleOutcomes() { }

}
