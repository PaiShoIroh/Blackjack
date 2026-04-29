package com.aditya.blackjack.engine;

public enum RoundOutcome {
    WIN,
    LOSE,
    PUSH,       // tie
    BLACKJACK,  // 3:2 payout
    SURRENDER   // half bet returned
}