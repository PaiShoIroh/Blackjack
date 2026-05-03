package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import com.aditya.blackjack.domain.card.Suit;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.player.PlayerAction;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.table.TableConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class GameTest {

    private TableConfig config;
    private Player player;
    private static final RoundResultListener NO_OP_LISTENER = outcomes -> {};

    @BeforeEach
    void setUp() {
        config = new TableConfig(6, 7, 10, 1000, false);
        player = new Player("aditya", 500);
    }

    private void loadShoe(Game game, Card... cards) {
        List<Card> padded = new ArrayList<>(List.of(cards));
        for (int i = 0; i < 20; i++) padded.add(new Card(Rank.FIVE, Suit.CLUBS));
        game.getTable().getShoe().loadCards(padded);
    }

    // always stand, always bet minimum
    private Game buildGame(PlayerAction action) {
        return new Game(config,
                (seat, hand) -> action,
                seats -> seats.stream()
                        .collect(Collectors.toMap(s -> s, s -> 10)), NO_OP_LISTENER);
    }

    // -------------------------------------------------------------------------

    @Test
    void addPlayerAssignsToCorrectSeat() {
        Game game = buildGame(PlayerAction.STAND);
        game.addPlayer(player, 1);
        assertThat(game.getTable().getSeat(1).get().getPlayer()).isEqualTo(player);
    }

    @Test
    void addPlayerToInvalidSeatThrows() {
        Game game = buildGame(PlayerAction.STAND);
        assertThatThrownBy(() -> game.addPlayer(player, 99))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gameStopsWhenPlayerCannotAffordMinimumBet() {
        Player broke = new Player("broke", 5); // below minimum bet of 10
        Game game = buildGame(PlayerAction.STAND);
        game.addPlayer(broke, 1);
        loadShoe(game,
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.DIAMONDS),
                new Card(Rank.NINE, Suit.SPADES)
        );
        game.start();
        assertThat(game.isRunning()).isFalse();
    }

    @Test
    void gameStopsWhenNoPlayersSeated() {
        Game game = buildGame(PlayerAction.STAND);
        // no players added
        game.start();
        assertThat(game.isRunning()).isFalse();
    }

    @Test
    void gameStopsWhenBetProviderReturnsEmpty() {
        Game game = new Game(config,
                (seat, hand) -> PlayerAction.STAND,
                seats -> Map.of(), NO_OP_LISTENER); // always return empty bets
        game.addPlayer(player, 1);
        game.start();
        assertThat(game.isRunning()).isFalse();
    }

    @Test
    void singleRoundCompletesAndUpdatesBalance() {
        Game game = new Game(config,
                (seat, hand) -> PlayerAction.STAND,
                seats -> {
                    // bet once then return empty to stop the loop
                    Map<Seat, Integer> bets = seats.stream()
                            .collect(java.util.stream.Collectors.toMap(s -> s, s -> 10));
                    return bets;
                }, NO_OP_LISTENER);
        game.addPlayer(player, 1);

        loadShoe(game,
                new Card(Rank.TEN, Suit.HEARTS),   // seat card 1
                new Card(Rank.TWO, Suit.CLUBS),    // dealer card 1
                new Card(Rank.NINE, Suit.DIAMONDS),// seat card 2 → 19
                new Card(Rank.SIX, Suit.SPADES),   // dealer card 2 → 8, hits
                new Card(Rank.TEN, Suit.HEARTS)    // dealer hits → 18
        );

        // run just one round by stopping after first play
        game.getTable().getOccupiedSeats();
        Round round = new Round(game.getTable(), (s, h) -> PlayerAction.STAND);
        round.collectBets(Map.of(game.getTable().getSeat(1).get(), 10));
        round.deal();
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();

        // player had 19 vs dealer 18 — should win
        assertThat(player.getBalance()).isGreaterThan(490); // at least got something back
    }

    @Test
    void stopHaltsGameLoop() {
        Game game = buildGame(PlayerAction.STAND);
        game.addPlayer(new Player("aditya", 500), 1);

        loadShoe(game,
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.DIAMONDS),
                new Card(Rank.NINE, Suit.SPADES)
        );

        // stop immediately after first round via a thread
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            game.stop();
        });
        stopper.start();
        game.start();
        assertThat(game.isRunning()).isFalse();
    }
}