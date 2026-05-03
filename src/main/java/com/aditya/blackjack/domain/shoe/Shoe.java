package com.aditya.blackjack.domain.shoe;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import com.aditya.blackjack.domain.card.Suit;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
The Shoe has three responsibilities:
- Build N standard decks of 52 cards
- Shuffle them
- Track the cut card and signal when reshuffle is needed
* */
public class Shoe {

    private final int numberOfDecks;
    private List<Card> cards;
    @Getter
    private int cutCardPosition;

    public Shoe(int numberOfDecks) {
        this.numberOfDecks = numberOfDecks;
        this.cards = new ArrayList<>();
        build();
        shuffle();
    }

    private void build() {
        cards.clear();
        for (int i = 0; i < numberOfDecks; i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(rank, suit));
                }
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
        // cut card placed at 75% through the shoe
        cutCardPosition = (int) (cards.size() * 0.75);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Shoe is empty - reshuffle required");
        }
        return cards.removeFirst();
    }

    public boolean needsShuffle() {
        return remainingCards() < cutCardPosition;
    }

    public int remainingCards() {
        return cards.size();
    }

    public int totalCards() {
        return numberOfDecks * 52;
    }

    public void reset() {
        build();
        shuffle();
    }

    // test hook — loads a fixed sequence of cards to the front of the shoe
    public void loadCards(List<Card> fixedCards) {
        this.cards = new ArrayList<>(fixedCards);
    }
}
