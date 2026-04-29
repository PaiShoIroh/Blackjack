package com.aditya.blackjack.domain.player;

import lombok.Getter;

@Getter
public class Player {

    private final String username;
    private int balance;

    public Player(String username, int initialBalance) {
        if (initialBalance < 0) throw new IllegalArgumentException("Initial balance cannot be negative");
        this.username = username;
        this.balance = initialBalance;
    }

    public void debit(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Debit amount must be positive");
        if (amount > balance) throw new IllegalStateException("Insufficient balance");
        balance -= amount;
    }

    public void credit(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Credit amount must be positive");
        balance += amount;
    }
}
