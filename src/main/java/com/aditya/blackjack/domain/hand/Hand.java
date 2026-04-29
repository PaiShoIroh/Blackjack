package com.aditya.blackjack.domain.hand;

import com.aditya.blackjack.domain.card.Card;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor(force = true)
@Getter
public class Hand {

    private final List<Card> cards;
    @Setter
    private HandStatus status;

    public void addCard(Card card) {}
    public int getValue() {return 0;}
    public boolean isBlackjack() { return false; }
    public boolean isBust() { return false; }
    public boolean canSplit() { return false; }  // two cards, same rank value
    public boolean canDoubleDown() { return false; }


}
