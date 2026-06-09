package com.aditya.blackjack.api.dto;

import com.aditya.blackjack.engine.RoundPhase;

import java.util.List;
import java.util.Map;

public record GameStateResponse(
        RoundPhase phase,
        String dealerUpCard,
        int dealerValue,
        Map<Integer, SeatState> seats
) {
    public record SeatState(
            String username,
            List<String> cards,
            int handValue,
            String handStatus,
            int bet,
            int balance
    ) {}
}
