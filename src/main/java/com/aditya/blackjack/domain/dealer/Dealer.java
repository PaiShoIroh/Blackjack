package com.aditya.blackjack.domain.dealer;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.hand.Hand;

public class Dealer {
    private Hand hand;

    public Dealer() { }

    public void receiveCard(Card card) { }
    public boolean shouldHit() { return false; } // hits on <17, configurable soft-17
    public Hand getHand() { return hand; }
    public void reset() { } // clear hand between rounds
}
