package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.seat.Seat;
import java.util.Map;

@FunctionalInterface
public interface BetProvider {
    Map<Seat, Integer> getBets(java.util.List<Seat> occupiedSeats);
}