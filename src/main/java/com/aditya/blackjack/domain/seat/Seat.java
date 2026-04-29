package com.aditya.blackjack.domain.seat;

import com.aditya.blackjack.domain.hand.Hand;
import com.aditya.blackjack.domain.player.Player;
import lombok.Getter;

@Getter
public class Seat {
    private final int id;
    private Player player;
    private Hand hand;
    private int bet;
    private SeatStatus status;

    private Seat(int id) { this.id = id; }

    private void assignPlayer(Player player) { this.player = player; }
    private void removePlayer() {}
    private void placeBet(int amount) { this.bet = amount; }
    private void clearHand() {}
    private boolean isEmpty() {return false;}

}
