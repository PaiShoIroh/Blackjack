package com.aditya.blackjack.engine;

import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.table.Table;
import com.aditya.blackjack.domain.table.TableConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Getter
@Slf4j
public class Game {
    @Getter
    private final Table table;
    private final ActionProvider actionProvider;
    private final BetProvider betProvider;
    private final RoundResultListener resultListener;
    private boolean running;


    public Game(TableConfig config, ActionProvider actionProvider, BetProvider betProvider, RoundResultListener resultListener) {
        this.table = new Table(config);
        this.actionProvider = actionProvider;
        this.betProvider = betProvider;
        this.running = false;
        this.resultListener = resultListener;
    }

    public void addPlayer(Player player, int seatId) {
        table.getSeat(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat" + seatId + "does not exist"))
                .assignPlayer(player);
    }

    public void start() {
        running = true;
        while (running) {
            List<Seat> occupiedSeats = table.getOccupiedSeats();
            if (occupiedSeats.isEmpty()) {
                log.info("No players are currently playing. Game over.");
                running = false;
                break;
            }

            // check if any player can still afford minimum bet
            boolean anyPlayerCanBet = occupiedSeats.stream()
                    .anyMatch(seat -> seat.getPlayer().getBalance() >= table.getConfig().getMinimumBet());
            if (!anyPlayerCanBet) {
                log.info("All players cannot afford minimum bet. Game over.");
                running = false;
                break;
            }

            playRound();
            table.resetForNewRound();
        }
    }

    public void stop() {
        running = false;
    }

    private void playRound() {
        Map<Seat, Integer> bets = betProvider.getBets(table.getOccupiedSeats());
        if (bets.isEmpty()) {
            log.info("No players are currently betting.");
            running = false;
            return;
        }

        Round round = new Round(table, actionProvider);
        round.collectBets(bets);
        round.deal();
        if (round.getPhase() == RoundPhase.COMPLETE) {
            // dealer blackjack — round auto-settled
            return;
        }
        round.playSeats();
        round.playDealer();
        round.settleOutcomes();
        resultListener.onRoundComplete(round.getOutcomes());
    }
}
