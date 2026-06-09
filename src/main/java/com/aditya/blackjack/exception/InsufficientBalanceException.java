package com.aditya.blackjack.exception;

public class InsufficientBalanceException extends GameException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
