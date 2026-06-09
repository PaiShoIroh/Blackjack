package com.aditya.blackjack.domain.player;

import com.aditya.blackjack.exception.GameException;
import com.aditya.blackjack.exception.InsufficientBalanceException;
import lombok.Getter;

@Getter
public class Player {

    private final String username;
    private int balance;

    public Player(String username, int initialBalance) {
        if (initialBalance < 0) throw new GameException("Initial balance cannot be negative");
        this.username = username;
        this.balance = initialBalance;
    }

    public void debit(int amount) {
        if (amount <= 0) throw new GameException("Debit amount must be positive");
        if (amount > balance) throw new InsufficientBalanceException("Insufficient balance ($" + balance + ") for debit of $" + amount);
        balance -= amount;
    }

    public void credit(int amount) {
        if (amount <= 0) throw new GameException("Credit amount must be positive");
        balance += amount;
    }
}
