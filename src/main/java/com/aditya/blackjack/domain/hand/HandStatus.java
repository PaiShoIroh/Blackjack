package com.aditya.blackjack.domain.hand;

public enum HandStatus {
    ACTIVE,       // still in play
    STOOD,        // player chose to stand
    BUST,         // exceeded 21
    BLACKJACK,    // natural 21 on first two cards
    SURRENDERED   // player surrendered
}