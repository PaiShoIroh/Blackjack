package com.aditya.blackjack.domain.seat;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.hand.Hand;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.table.TableConfig;
import lombok.Getter;

@Getter
public class Seat {
    private final int id;
    private final TableConfig config;
    private Player player;
    private Hand hand;
    private int bet;
    private SeatStatus status;

    public Seat(int id, TableConfig config) {
        this.id = id;
        this.config = config;
        this.status = SeatStatus.EMPTY;
    }

    public void assignPlayer(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null");
        if (status != SeatStatus.EMPTY)
            throw new IllegalStateException("Seat is already occupied");
        this.player = player;
        this.status = SeatStatus.OCCUPIED;
    }

    public void removePlayer() {
        if (status == SeatStatus.ACTIVE) throw new IllegalStateException("Cannot remove player during active hand");
        this.player = null;
        this.bet = 0;
        this.hand = null;
        this.status = SeatStatus.EMPTY;
    }

    public void placeBet(int amount) {
        if (status != SeatStatus.OCCUPIED) throw new IllegalStateException("Seat must be occupied to place a bet");
        if (amount < config.getMinimumBet()) throw new IllegalArgumentException("Bet below table minimum");
        if (amount > config.getMaximumBet()) throw new IllegalArgumentException("Bet above table maximum");
        if (amount > player.getBalance()) throw new IllegalStateException("Insufficient player balance");
        if (amount % 2 != 0) throw new IllegalArgumentException("Bet must be even");
        this.bet = amount;
        this.status = SeatStatus.ACTIVE;
        player.debit(amount);
    }

    public void dealCard(Card card) {
        if (hand == null) hand = new Hand();
        hand.addCard(card);
    }

    public void clearHand() {
        this.hand = null;
        this.bet = 0;
        if (status == SeatStatus.ACTIVE)
            this.status = SeatStatus.OCCUPIED;
    }

    public boolean isEmpty() {
        return status == SeatStatus.EMPTY;
    }

    public boolean isActive() {
        return status == SeatStatus.ACTIVE;
    }

    public void doubleBet() {
        this.bet *= 2;
    }

}
