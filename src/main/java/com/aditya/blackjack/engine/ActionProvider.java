package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.hand.Hand;
import com.aditya.blackjack.domain.player.PlayerAction;
import com.aditya.blackjack.domain.seat.Seat;

@FunctionalInterface
public interface ActionProvider {
    PlayerAction getAction(Seat seat, Hand hand);
}
