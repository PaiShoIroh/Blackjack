package com.aditya.blackjack.domain.seat;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import com.aditya.blackjack.domain.card.Suit;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.table.TableConfig;
import com.aditya.blackjack.exception.InsufficientBalanceException;
import com.aditya.blackjack.exception.InvalidBetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SeatTest {
    private TableConfig config;
    private Player player;
    private Seat seat;

    @BeforeEach
    void setUp() {
        config = new TableConfig(6, 7, 10, 100, false);
        player = new Player("aditya", 500);
        seat = new Seat(1, config);

    }

    @Test
    void newSeatIsEmpty() {
        assertThat(seat.isEmpty()).isTrue();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.EMPTY);
    }

    @Test
    void assignPlayerOccupiesSeat() {
        seat.assignPlayer(player);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.OCCUPIED);
        assertThat(seat.getPlayer()).isEqualTo(player);
    }

    @Test
    void assignNullPlayerThrows() {
        assertThatThrownBy(() -> seat.assignPlayer(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void assignPlayerToOccupiedSeatThrows() {
        seat.assignPlayer(player);
        assertThatThrownBy(() -> seat.assignPlayer(new Player("other", 100)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removePlayerEmptiesSeat() {
        seat.assignPlayer(player);
        seat.removePlayer();
        assertThat(seat.isEmpty()).isTrue();
        assertThat(seat.getPlayer()).isNull();
    }

    @Test
    void removePlayerDuringActiveHandThrows() {
        seat.assignPlayer(player);
        seat.placeBet(100);
        assertThatThrownBy(() -> seat.removePlayer())
                .isInstanceOf(IllegalStateException.class);
    }

    // --- betting ---

    @Test
    void placeBetDebitsPlayerAndActivatesSeat() {
        seat.assignPlayer(player);
        seat.placeBet(100);
        assertThat(seat.getBet()).isEqualTo(100);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.ACTIVE);
        assertThat(player.getBalance()).isEqualTo(400);
    }

    @Test
    void betBelowMinimumThrows() {
        seat.assignPlayer(player);
        assertThatThrownBy(() -> seat.placeBet(5))
                .isInstanceOf(InvalidBetException.class)
                .hasMessageContaining("minimum");
    }

    @Test
    void betAboveMaximumThrows() {
        seat.assignPlayer(player);
        assertThatThrownBy(() -> seat.placeBet(1500))
                .isInstanceOf(InvalidBetException.class)
                .hasMessageContaining("maximum");
    }

    @Test
    void betAboveBalanceThrows() {
        seat.assignPlayer(player);
        // max bet is 100, balance is 500 — bet of 600 hits max limit first
        assertThatThrownBy(() -> seat.placeBet(600))
                .isInstanceOf(InvalidBetException.class)
                .hasMessageContaining("maximum");
    }

    @Test
    void betOnEmptySeatThrows() {
        assertThatThrownBy(() -> seat.placeBet(100))
                .isInstanceOf(InvalidBetException.class);
    }

    @Test
    void betExceedingBalanceButWithinLimitsThrows() {
        Player poorPlayer = new Player("poor", 20);
        Seat poorSeat = new Seat(2, config);
        poorSeat.assignPlayer(poorPlayer);
        assertThatThrownBy(() -> poorSeat.placeBet(30))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void oddBetThrows() {
        seat.assignPlayer(player);
        assertThatThrownBy(() -> seat.placeBet(11))
                .isInstanceOf(InvalidBetException.class)
                .hasMessageContaining("even");
    }

    // --- dealing cards ---

    @Test
    void dealCardCreatesHandIfAbsent() {
        seat.assignPlayer(player);
        seat.placeBet(100);
        seat.dealCard(new Card(Rank.ACE, Suit.SPADES));
        assertThat(seat.getHand()).isNotNull();
        assertThat(seat.getHand().getCards()).hasSize(1);
    }

    // --- clearHand ---

    @Test
    void clearHandResetsToOccupied() {
        seat.assignPlayer(player);
        seat.placeBet(100);
        seat.dealCard(new Card(Rank.TEN, Suit.HEARTS));
        seat.clearHand();
        assertThat(seat.getHand()).isNull();
        assertThat(seat.getBet()).isEqualTo(0);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.OCCUPIED);
    }

}
