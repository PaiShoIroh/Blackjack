package com.aditya.blackjack.domain.table;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import com.aditya.blackjack.domain.card.Suit;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.shoe.Shoe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TableTest {

    private TableConfig config;
    private Table table;

    @BeforeEach
    void setUp() {
        config = new TableConfig(6, 7, 10, 1000, true);
        table = new Table(config);
    }

    // --- initialisation ---

    @Test
    void tableInitialisesCorrectNumberOfSeats() {
        assertThat(table.getSeats()).hasSize(7);
    }

    @Test
    void allSeatsAreEmptyOnInit() {
        assertThat(table.getSeats()).allMatch(Seat::isEmpty);
    }

    @Test
    void shoeIsInitialisedWithCorrectDecks() {
        // 6 decks * 52 cards
        assertThat(table.getShoe().remainingCards()).isEqualTo(312);
    }

    @Test
    void dealerIsInitialisedWithEmptyHand() {
        assertThat(table.getDealer().getHand().getCards()).isEmpty();
    }

    // --- getSeat ---

    @Test
    void getSeatByValidIdReturnsCorrectSeat() {
        assertThat(table.getSeat(1)).isPresent();
        assertThat(table.getSeat(1).get().getId()).isEqualTo(1);
    }

    @Test
    void getSeatByInvalidIdReturnsEmpty() {
        assertThat(table.getSeat(99)).isEmpty();
    }

    // --- occupied / active seats ---

    @Test
    void noOccupiedSeatsInitially() {
        assertThat(table.getOccupiedSeats()).isEmpty();
    }

    @Test
    void occupiedSeatsReflectsAssignedPlayers() {
        Player player = new Player("aditya", 500);
        table.getSeat(1).get().assignPlayer(player);
        table.getSeat(3).get().assignPlayer(new Player("other", 500));
        assertThat(table.getOccupiedSeats()).hasSize(2);
    }

    @Test
    void activeSeatsReflectsBettedSeats() {
        Player player = new Player("aditya", 500);
        table.getSeat(1).get().assignPlayer(player);
        table.getSeat(1).get().placeBet(100);
        assertThat(table.getActiveSeats()).hasSize(1);
    }

    // --- resetForNewRound ---

    @Test
    void resetClearsDealerHand() {
        table.getDealer().receiveCard(table.getShoe().draw());
        table.resetForNewRound();
        assertThat(table.getDealer().getHand().getCards()).isEmpty();
    }

    @Test
    void resetClearsAllSeatHands() {
        Player player = new Player("aditya", 500);
        Seat seat = table.getSeat(1).get();
        seat.assignPlayer(player);
        seat.placeBet(100);
        seat.dealCard(table.getShoe().draw());
        table.resetForNewRound();
        assertThat(seat.getHand()).isNull();
        assertThat(seat.getBet()).isEqualTo(0);
    }

    @Test
    void resetRebuildsShoeWhenNeeded() {
        int fullSize = table.getShoe().remainingCards(); // 312
        while (!table.getShoe().needsShuffle()) {
            table.getShoe().draw();
        }
        assertThat(table.getShoe().remainingCards()).isLessThan(fullSize);
        table.resetForNewRound();
        assertThat(table.getShoe().remainingCards()).isEqualTo(fullSize);
    }

    // --- getDealerUpCard ---

    @Test
    void getDealerUpCardReturnsFirstCard() {
        Card card = new Card(Rank.ACE, Suit.SPADES);
        table.getDealer().receiveCard(card);
        table.getDealer().receiveCard(new Card(Rank.TEN, Suit.HEARTS));
        assertThat(table.getDealerUpCard()).isEqualTo(card);
    }

    @Test
    void getDealerUpCardWithNoCardsThrows() {
        assertThatThrownBy(() -> table.getDealerUpCard())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resetRebuildsFullShoe() {
        Shoe shoe = new Shoe(6);
        for (int i = 0; i < 100; i++) shoe.draw();
        assertThat(shoe.remainingCards()).isEqualTo(212);
        shoe.reset();
        assertThat(shoe.remainingCards()).isEqualTo(312);
    }
}