package com.aditya.blackjack.exception;

import com.aditya.blackjack.engine.RoundPhase;

public class InvalidPhaseException extends GameException {
    public InvalidPhaseException(RoundPhase expected, RoundPhase actual) {
        super("Expected phase " + expected + " but was " + actual);
    }
}
