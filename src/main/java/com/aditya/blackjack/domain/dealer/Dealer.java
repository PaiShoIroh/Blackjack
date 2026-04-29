package com.aditya.blackjack.domain.dealer;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.hand.Hand;
import lombok.Getter;

@Getter
public class Dealer {

    private Hand hand;
    private final boolean hitOnSoft17;

    public Dealer(boolean hitOnSoft17) {
        this.hitOnSoft17 = hitOnSoft17;
        this.hand = new Hand();
    }

    public void receiveCard(Card card) {
        hand.addCard(card);
    }

    public boolean shouldHit() {
        int value = hand.getValue();
        if (value < 17) return true;
        return value == 17 && hand.isSoft() && hitOnSoft17;
    }

    public void reset() {
        hand = new Hand();
    }
}
