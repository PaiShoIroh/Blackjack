package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.card.Rank;
import com.aditya.blackjack.domain.card.Suit;
import com.aditya.blackjack.domain.hand.HandStatus;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.player.PlayerAction;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.table.Table;
import com.aditya.blackjack.domain.table.TableConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class RoundTest {

    private Table table;
    private Player player;
    private Seat seat;

    @BeforeEach
    void setUp() {
        TableConfig config = new TableConfig(6, 7, 10, 1000, false);
        table = new Table(config);
        player = new Player("aditya", 1000);
        seat = table.getSeat(1).get();
        seat.assignPlayer(player);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    // loads a fixed sequence of cards into the shoe (drawn front to back)
    private void loadShoe(Card... cards) {
        List<Card> padded = new ArrayList<>(List.of(cards));
        // pad with neutral cards so dealer hits never exhaust the shoe
        for (int i = 0; i < 20; i++) {
            padded.add(new Card(Rank.NINE, Suit.CLUBS));
        }
        table.getShoe().loadCards(padded);
    }

    private Round startRound(PlayerAction... actions) {
        int[] index = {0};
        ActionProvider provider = (s, h) -> actions[index[0]++];
        Round round = new Round(table, provider);
        round.collectBets(Map.of(seat, 100));
        return round;
    }

    // -------------------------------------------------------------------------
    // dealing
    // -------------------------------------------------------------------------

    @Test
    void dealGivesTwoCardsToSeatAndDealer() {
        loadShoe(
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        assertThat(seat.getHand().getCards()).hasSize(2);
        assertThat(table.getDealer().getHand().getCards()).hasSize(2);
    }

    @Test
    void dealingAdvancesToPlayingPhase() {
        loadShoe(
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        assertThat(round.getPhase()).isEqualTo(RoundPhase.PLAYING);
    }

    // -------------------------------------------------------------------------
    // player actions
    // -------------------------------------------------------------------------

    @Test
    void playerHitAddsCard() {
        loadShoe(
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.EIGHT, Suit.SPADES),
                new Card(Rank.THREE, Suit.HEARTS)   // hit card
        );
        Round round = startRound(PlayerAction.HIT, PlayerAction.STAND);
        round.deal();
        round.playSeats();
        assertThat(seat.getHand().getCards()).hasSize(3);
    }

    @Test
    void playerStandSetsHandToStood() {
        loadShoe(
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        round.playSeats();
        assertThat(seat.getHand().getStatus()).isEqualTo(HandStatus.STOOD);
    }

    @Test
    void playerBustSetsHandToBust() {
        // load shoe so player gets 10, 10, then hits a 10 = bust
        loadShoe(
                new Card(Rank.TEN, Suit.HEARTS),   // seat card 1
                new Card(Rank.TEN, Suit.CLUBS),    // dealer card 1
                new Card(Rank.TEN, Suit.DIAMONDS), // seat card 2
                new Card(Rank.TWO, Suit.SPADES),   // dealer card 2
                new Card(Rank.TEN, Suit.SPADES)    // hit card
        );
        Round round = startRound(PlayerAction.HIT);
        round.deal();
        round.playSeats();
        assertThat(seat.getHand().getStatus()).isEqualTo(HandStatus.BUST);
    }

    @Test
    void playerSurrenderReturnsHalfBet() {
        loadShoe(
                new Card(Rank.NINE, Suit.HEARTS),   // seat card 1
                new Card(Rank.TEN, Suit.CLUBS),     // dealer card 1
                new Card(Rank.SIX, Suit.DIAMONDS),  // seat card 2 → 15, bad hand, surrender
                new Card(Rank.EIGHT, Suit.SPADES),  // dealer card 2 → 18
                new Card(Rank.FIVE, Suit.CLUBS)     // padding
        );
        Round round = startRound(PlayerAction.SURRENDER);
        round.deal();
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();
        assertThat(player.getBalance()).isEqualTo(950); // 1000 - 100 bet + 50 returned
    }

    @Test
    void playerDoubleDownDoublesBetAndTakesOneCard() {
        loadShoe(
                new Card(Rank.FIVE, Suit.HEARTS),   // seat card 1
                new Card(Rank.TEN, Suit.CLUBS),     // dealer card 1
                new Card(Rank.SIX, Suit.DIAMONDS),  // seat card 2 → 11, good double down
                new Card(Rank.EIGHT, Suit.SPADES),  // dealer card 2
                new Card(Rank.NINE, Suit.HEARTS),   // double down card → 20
                new Card(Rank.TEN, Suit.CLUBS)      // dealer hits if needed
        );
        Round round = startRound(PlayerAction.DOUBLE_DOWN);
        round.deal();
        round.playSeats();
        assertThat(seat.getBet()).isEqualTo(200);
        assertThat(seat.getHand().getCards()).hasSize(3);
        assertThat(seat.getHand().getStatus()).isIn(HandStatus.STOOD, HandStatus.BUST);
    }

    // -------------------------------------------------------------------------
    // blackjack
    // -------------------------------------------------------------------------

    @Test
    void playerBlackjackPaysThreeToTwo() {
        loadShoe(
                new Card(Rank.ACE, Suit.SPADES),   // seat card 1
                new Card(Rank.TWO, Suit.CLUBS),    // dealer card 1
                new Card(Rank.KING, Suit.HEARTS),  // seat card 2
                new Card(Rank.THREE, Suit.DIAMONDS)// dealer card 2
        );
        Round round = startRound();
        round.deal();
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();
        // 1000 - 100 bet + 250 (3:2 payout) = 1150
        assertThat(player.getBalance()).isEqualTo(1150);
    }

    @Test
    void dealerBlackjackPlayerLoses() {
        loadShoe(
                new Card(Rank.TWO, Suit.SPADES),   // seat card 1
                new Card(Rank.ACE, Suit.CLUBS),    // dealer card 1
                new Card(Rank.THREE, Suit.HEARTS), // seat card 2
                new Card(Rank.KING, Suit.DIAMONDS) // dealer card 2
        );
        Round round = startRound();
        round.deal();
        assertThat(round.getPhase()).isEqualTo(RoundPhase.COMPLETE);
        assertThat(round.getOutcomes().get(seat)).isEqualTo(RoundOutcome.LOSE);
        assertThat(player.getBalance()).isEqualTo(900);
    }

    @Test
    void dealerBlackjackPlayerBlackjackIsPush() {
        loadShoe(
                new Card(Rank.ACE, Suit.SPADES),   // seat card 1
                new Card(Rank.ACE, Suit.CLUBS),    // dealer card 1
                new Card(Rank.KING, Suit.HEARTS),  // seat card 2
                new Card(Rank.KING, Suit.DIAMONDS) // dealer card 2
        );
        Round round = startRound();
        round.deal();
        assertThat(round.getOutcomes().get(seat)).isEqualTo(RoundOutcome.PUSH);
        assertThat(player.getBalance()).isEqualTo(1000); // bet returned
    }

    // -------------------------------------------------------------------------
    // dealer play
    // -------------------------------------------------------------------------


    @Test
    void dealerHitsUntilSeventeen() {
        loadShoe(
                new Card(Rank.TEN, Suit.HEARTS),    // seat card 1
                new Card(Rank.TWO, Suit.CLUBS),     // dealer card 1
                new Card(Rank.EIGHT, Suit.DIAMONDS),// seat card 2 → 18, stands
                new Card(Rank.TWO, Suit.SPADES),    // dealer card 2 → 4, hits
                new Card(Rank.FIVE, Suit.HEARTS),   // dealer hits → 9, hits
                new Card(Rank.NINE, Suit.CLUBS)     // dealer hits → 18, stands
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        round.playSeats();
        round.playDealer();
        assertThat(table.getDealer().getHand().getValue()).isGreaterThanOrEqualTo(17);
    }

    @Test
    void dealerDoesNotPlayIfAllPlayersBust() {
        loadShoe(
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.THREE, Suit.SPADES),
                new Card(Rank.TEN, Suit.SPADES)    // hit card — busts player
        );
        Round round = startRound(PlayerAction.HIT);
        round.deal();
        round.playSeats();
        round.playDealer();
        // dealer should only have initial 2 cards
        assertThat(table.getDealer().getHand().getCards()).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // settlement
    // -------------------------------------------------------------------------

    @Test
    void playerWinsWhenHigherThanDealer() {
        loadShoe(
                new Card(Rank.TEN, Suit.HEARTS),   // seat card 1
                new Card(Rank.TEN, Suit.CLUBS),    // dealer card 1
                new Card(Rank.NINE, Suit.DIAMONDS),// seat card 2 → 19
                new Card(Rank.SIX, Suit.SPADES)    // dealer card 2 → 16, must hit
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();
        assertThat(round.getOutcomes().get(seat)).isEqualTo(RoundOutcome.WIN);
        assertThat(player.getBalance()).isEqualTo(1100); // 1000 - 100 + 200
    }

    @Test
    void playerLosesWhenLowerThanDealer() {
        loadShoe(
                new Card(Rank.TEN, Suit.HEARTS),   // seat card 1
                new Card(Rank.TEN, Suit.CLUBS),    // dealer card 1
                new Card(Rank.SIX, Suit.DIAMONDS), // seat card 2 → 16
                new Card(Rank.NINE, Suit.SPADES)   // dealer card 2 → 19
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();
        assertThat(round.getOutcomes().get(seat)).isEqualTo(RoundOutcome.LOSE);
        assertThat(player.getBalance()).isEqualTo(900);
    }

    @Test
    void pushReturnsBet() {
        loadShoe(
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.NINE, Suit.DIAMONDS), // seat → 19
                new Card(Rank.NINE, Suit.SPADES)    // dealer → 19
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();
        assertThat(round.getOutcomes().get(seat)).isEqualTo(RoundOutcome.PUSH);
        assertThat(player.getBalance()).isEqualTo(1000);
    }

    @Test
    void dealerBustPlayerWins() {
        loadShoe(
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.DIAMONDS),// seat → 18
                new Card(Rank.SIX, Suit.SPADES),    // dealer → 16
                new Card(Rank.TEN, Suit.HEARTS)     // dealer hits → bust
        );
        Round round = startRound(PlayerAction.STAND);
        round.deal();
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();
        assertThat(round.getOutcomes().get(seat)).isEqualTo(RoundOutcome.WIN);
        assertThat(player.getBalance()).isEqualTo(1100);
    }

    // -------------------------------------------------------------------------
    // phase guards
    // -------------------------------------------------------------------------

    @Test
    void callingDealBeforeBettingThrows() {
        loadShoe(
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );
        Round round = new Round(table, (s, h) -> PlayerAction.STAND);
        assertThatThrownBy(round::deal)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void callingPlaySeatsBeforeDealThrows() {
        loadShoe(
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.SIX, Suit.DIAMONDS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );
        Round round = startRound(PlayerAction.STAND);
        assertThatThrownBy(round::playSeats)
                .isInstanceOf(IllegalStateException.class);
    }
}