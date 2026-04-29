package com.aditya.blackjack.domain.shoe;

import com.aditya.blackjack.domain.card.Card;

import java.util.List;

public class Shoe {

    private int numberOfDecks; // TODO: make it final
    private List<Card> cards;
    private int cutCardPosition;

    public Shoe(int numberOfDecks) { }

    public void shuffle() { }
    public Card draw() { return null; }
    public boolean needsShuffle() { return false; } // past cut card position
    public int remainingCards() { return 0; }
}
