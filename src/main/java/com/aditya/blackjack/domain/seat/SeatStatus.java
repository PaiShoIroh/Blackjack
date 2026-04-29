package com.aditya.blackjack.domain.seat;

public enum SeatStatus {
    EMPTY,    // no player assigned
    OCCUPIED, // player sitting, no active hand
    ACTIVE    // hand in progress this round
}