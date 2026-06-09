package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.card.Card;
import com.aditya.blackjack.domain.hand.Hand;
import com.aditya.blackjack.domain.hand.HandStatus;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.player.PlayerAction;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.table.Table;
import com.aditya.blackjack.exception.InsufficientBalanceException;
import com.aditya.blackjack.exception.InvalidActionException;
import com.aditya.blackjack.exception.InvalidPhaseException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Round {
    private final Table table;
    private final ActionProvider actionProvider;
    private RoundPhase phase;

    // primary outcomes per seat
    private Map<Seat, RoundOutcome> outcomes = new LinkedHashMap<>();

    // split hands per seat - empty unless a split occurred
    private final Map<Seat, List<Hand>> splitHands = new LinkedHashMap<>();

    public Round(Table table, ActionProvider actionProvider) {
        this.table = table;
        this.actionProvider = actionProvider;
        this.phase = RoundPhase.BETTING;
    }

    // -------------------------------------------------------------------------
    // Phase: BETTING
    // -------------------------------------------------------------------------

    public void collectBets(Map<Seat, Integer> bets) {
        assertPhase(RoundPhase.BETTING);
        bets.forEach(Seat::placeBet);
        phase = RoundPhase.DEALING;
    }

    // -------------------------------------------------------------------------
    // Phase: DEALING
    // -------------------------------------------------------------------------
    public void deal() {
        assertPhase(RoundPhase.DEALING);
        List<Seat> activeSeats = table.getActiveSeats();

        // 2 cards each, seat order, dealer last
        for (int pass = 0; pass < 2; pass++) {
            for (Seat seat : activeSeats) {
                seat.dealCard(table.getShoe().draw());
            }
            table.getDealer().receiveCard(table.getShoe().draw());
        }

        if (table.getDealer().getHand().isBlackjack()) {
            settleForDealerBlackjack();
            phase = RoundPhase.COMPLETE;
            return;
        }

        phase = RoundPhase.PLAYING;
    }

    // -------------------------------------------------------------------------
    // Phase: PLAYING
    // -------------------------------------------------------------------------


    public void playSeats() {
        assertPhase(RoundPhase.PLAYING);
        for (Seat seat : table.getActiveSeats()) {
            Hand hand = seat.getHand();

            // case 1: player has a blackjack
            if (hand.isBlackjack()) {
                hand.setStatus(HandStatus.BLACKJACK);
                continue;
            }

            playHand(seat, hand);

            // play any split hands
            List<Hand> splits = splitHands.getOrDefault(seat, Collections.emptyList());
            for (Hand splitHand : splits) {
                if (splitHand.getStatus() == HandStatus.ACTIVE) {
                    playHand(seat, splitHand);
                }
            }
        }
        phase = RoundPhase.DEALER;
    }

    private void playHand(Seat seat, Hand hand) {
        while (hand.getStatus() == HandStatus.ACTIVE) {
            PlayerAction action = actionProvider.getAction(seat, hand);

            switch (action) {
                case HIT -> {
                    hand.addCard(table.getShoe().draw());
                    if (hand.isBust()) {
                        hand.setStatus(HandStatus.BUST);
                    }
                }
                case STAND -> hand.setStatus(HandStatus.STOOD);
                case DOUBLE_DOWN -> {
                    if (!hand.canDoubleDown()) throw new InvalidActionException("Cannot double down — only allowed on first two cards");
                    int extraBet = seat.getBet();
                    if (seat.getPlayer().getBalance() < extraBet) {
                        throw new InsufficientBalanceException("Insufficient balance to double down (need $" + extraBet + ")");
                    }
                    seat.getPlayer().debit(extraBet);
                    seat.doubleBet();
                    hand.addCard(table.getShoe().draw());
                    hand.setStatus(hand.isBust() ? HandStatus.BUST : HandStatus.STOOD);
                }
                case SPLIT -> {
                    if (!hand.canSplit()) throw new InvalidActionException("Cannot split — cards must be a pair");
                    if (seat.getPlayer().getBalance() < seat.getBet())
                        throw new InsufficientBalanceException("Insufficient balance to split (need $" + seat.getBet() + ")");
                    performSplit(seat, hand);
                }
                case SURRENDER -> {
                    if (!hand.canSurrender()) throw new InvalidActionException("Cannot surrender — only allowed on first two cards");
                    hand.setStatus(HandStatus.SURRENDERED);
                }
            }
        }
    }

    private void performSplit(Seat seat, Hand originalHand) {
        // remove second card from the original hand
        Card splitCard = originalHand.split();

        // create a new hand with the split card
        Hand newHand = new Hand();
        newHand.addCard(splitCard);

        // deal one new card to each hand
        originalHand.addCard(table.getShoe().draw());
        newHand.addCard(table.getShoe().draw());

        // debit player for the additional split bet
        seat.getPlayer().debit(seat.getBet());

        // track the split hand
        splitHands.computeIfAbsent(seat, k -> new ArrayList<>()).add(newHand);
    }


    // -------------------------------------------------------------------------
    // Phase: DEALER
    // -------------------------------------------------------------------------


    public void playDealer() {
        assertPhase(RoundPhase.DEALER);

        // dealer only plays if at least one player hand is still live
        boolean anyLive = table.getActiveSeats().stream()
                .anyMatch(seat -> isLiveHand(seat.getHand()) ||
                        splitHands.getOrDefault(seat, Collections.emptyList()).stream().anyMatch(this::isLiveHand));

        if (anyLive) {
            while (table.getDealer().shouldHit()) {
                table.getDealer().receiveCard(table.getShoe().draw());
            }
        }
        phase = RoundPhase.SETTLING;
    }

    private boolean isLiveHand(Hand hand) {
        return hand != null &&
                hand.getStatus() != HandStatus.BUST &&
                hand.getStatus() != HandStatus.SURRENDERED;
    }


    // -------------------------------------------------------------------------
    // Phase: SETTLING
    // -------------------------------------------------------------------------

    public void settleOutcomes() {
        assertPhase(RoundPhase.SETTLING);

        int dealerValue = table.getDealer().getHand().getValue();
        boolean dealerBust = table.getDealer().getHand().isBust();

        for (Seat seat : table.getActiveSeats()) {
            RoundOutcome outcome = resolveOutcome(seat.getHand(), dealerValue, dealerBust, false);
            outcomes.put(seat, outcome);
            applyPayout(seat.getPlayer(), seat.getBet(), outcome);

            // settle split hands
            for (Hand splitHand : splitHands.getOrDefault(seat, Collections.emptyList())) {
                RoundOutcome splitOutcome = resolveOutcome(splitHand, dealerValue, dealerBust, true);
                applyPayout(seat.getPlayer(), seat.getBet(), splitOutcome);
            }
        }
        phase = RoundPhase.COMPLETE;
    }

    private void settleForDealerBlackjack() {
        for (Seat seat : table.getActiveSeats()) {
            Hand hand = seat.getHand();
            if (hand != null && hand.isBlackjack()) {
                outcomes.put(seat, RoundOutcome.PUSH);
                seat.getPlayer().credit(seat.getBet()); // return bet
            } else {
                outcomes.put(seat, RoundOutcome.LOSE);
                // bet already debited, nothing returned
            }
        }
    }

    private void applyPayout(Player player, int bet, RoundOutcome outcome) {
        switch (outcome) {
            case WIN -> player.credit(bet * 2);
            case BLACKJACK -> player.credit((bet * 5) / 2);
            case PUSH -> player.credit(bet);
            case SURRENDER -> player.credit(bet / 2);
            case LOSE -> {
            } // nothing - bet is already debited
        }
    }

    private RoundOutcome resolveOutcome(Hand hand, int dealerValue, boolean dealerBust, boolean isSplitHand) {
        if (hand == null)
            return RoundOutcome.LOSE;

        return switch (hand.getStatus()) {
            case BUST -> RoundOutcome.LOSE;
            case SURRENDERED -> RoundOutcome.SURRENDER;
            case BLACKJACK -> isSplitHand ? RoundOutcome.WIN : RoundOutcome.BLACKJACK; // split blackjacks pays 1:1
            case STOOD, ACTIVE -> {
                int playerValue = hand.getValue();
                if (dealerBust || playerValue > dealerValue) yield RoundOutcome.WIN;
                if (playerValue == dealerValue) yield RoundOutcome.PUSH;
                yield RoundOutcome.LOSE;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void assertPhase(RoundPhase expected) {
        if (phase != expected) {
            throw new InvalidPhaseException(expected, phase);
        }
    }

}
