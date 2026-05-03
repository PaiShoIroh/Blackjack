package com.aditya.blackjack.domain.table;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.dealer.Dealer;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.shoe.Shoe;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
public class Table {
    private final TableConfig config;
    private final List<Seat> seats;
    private final Shoe shoe;
    private final Dealer dealer;

    public Table(TableConfig config) {
        this.config = config;
        shoe = new Shoe(config.getNumberOfDecks());
        dealer = new Dealer(config.isHitOnSoft17());
        seats = initialiseSeats(config.getNumberOfSeats());
    }

    private List<Seat> initialiseSeats(int numberOfSeats) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < numberOfSeats; i++) {
            seats.add(new Seat(i, config));
        }
        return Collections.unmodifiableList(seats);
    }

    public Optional<Seat> getSeat(int id) {
        return seats.stream().filter(seat -> seat.getId() == id).findFirst();
    }

    public List<Seat> getOccupiedSeats() {
        return seats.stream()
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public List<Seat> getActiveSeats() {
        return seats.stream()
                .filter(Seat::isActive)
                .toList();
    }

    public void resetForNewRound() {
        if (shoe.needsShuffle())
            shoe.reset();
        dealer.reset();
        seats.forEach(Seat::clearHand);
    }

    public Card getDealerUpCard() {
        List<Card> cards = dealer.getHand().getCards();
        if (cards.isEmpty()) throw new IllegalStateException("Dealer has no cards yet");
        return cards.getFirst();
    }
}
