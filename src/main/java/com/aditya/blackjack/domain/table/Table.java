package com.aditya.blackjack.domain.table;

import com.aditya.blackjack.domain.dealer.Dealer;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.shoe.Shoe;
import lombok.Getter;

import java.util.List;

@Getter
public class Table {
    private final TableConfig config;
    private final List<Seat> seats;
    private final Shoe shoe;
    private final Dealer dealer;

    public Table(TableConfig config) {
        seats = List.of();
        this.config = config;
        shoe = null;
        dealer = null;
    }

    public List<Seat> getOccupiedSeats() { return null; } // seats with players
    public Seat getSeat(int id) { return null; }
}
