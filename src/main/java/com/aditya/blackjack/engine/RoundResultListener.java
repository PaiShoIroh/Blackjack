package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.seat.Seat;
import java.util.Map;

@FunctionalInterface
public interface RoundResultListener {
    void onRoundComplete(Map<Seat, RoundOutcome> outcomes);
}