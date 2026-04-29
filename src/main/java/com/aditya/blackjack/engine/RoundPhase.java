package com.aditya.blackjack.engine;

public enum RoundPhase {
    BETTING,    // players place bets
    DEALING,    // initial 2-card deal
    PLAYING,    // players act on their hands
    DEALER,     // dealer reveals and acts
    SETTLING,   // bets paid out / collected
    COMPLETE    // round over
}