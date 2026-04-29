package com.aditya.blackjack.domain.player;

import lombok.Getter;

@Getter
public class Player {
    private final String username;
    private int balance;

    public Player(String username, int initialBalance) {
        this.username = "";
    }
    public void debit(int amount) { }   // place bet / deduct
    public void credit(int amount) { } // winnings / return bet
}
