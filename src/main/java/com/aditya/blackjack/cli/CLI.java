package com.aditya.blackjack.cli;

import com.aditya.blackjack.domain.hand.Hand;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.player.PlayerAction;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.table.Table;
import com.aditya.blackjack.domain.table.TableConfig;
import com.aditya.blackjack.engine.*;
import com.aditya.blackjack.exception.GameException;

import java.util.*;

public class CLI {

    private final Scanner scanner = new Scanner(System.in);
    private Table table;


    public static void main(String[] args) {
        new CLI().run();
    }

    public void run() {
        printWelcome();

        // --- setup ---
        String username = prompt("Enter your username: ");
        int balance = promptInt("Enter your starting balance: ", 1, Integer.MAX_VALUE);
        int seatId = promptInt("Choose a seat (1-7): ", 1, 7);

        TableConfig config = new TableConfig(6, 7, 10, 1000, true);
        Player player = new Player(username, balance);

        Game game = new Game(
                config,
                this::getAction,
                this::getBets,
                this::onRoundComplete
        );
        this.table = game.getTable();

        try {
            game.addPlayer(player, seatId);
        } catch (GameException e) {
            print("Error: " + e.getMessage());
            return;
        }

        print("\nWelcome to the table, " + username + "! Minimum bet: $" + config.getMinimumBet()
                + ", Maximum bet: $" + config.getMaximumBet());
        print("Type 'quit' at any time to leave the table.\n");

        game.start();

        print("\n--- You have left the table ---");
        print("Final balance: $" + player.getBalance());
        print("Thanks for playing, " + username + "!");
    }

    // -------------------------------------------------------------------------
    // BetProvider
    // -------------------------------------------------------------------------

    private Map<Seat, Integer> getBets(List<Seat> occupiedSeats) {
        if (quitRequested) return Map.of();
        Map<Seat, Integer> bets = new LinkedHashMap<>();

        print("\n============================================================");
        for (Seat seat : occupiedSeats) {
            Player player = seat.getPlayer();
            int bet = promptBet(seat, player);
            if (bet == -1) {
                print("Leaving the table...");
                return Map.of();
            }
            if (bet > 0) {
                bets.put(seat, bet);
            }
        }

        return bets;
    }

    private int promptBet(Seat seat, Player player) {
        while (true) {
            print("Balance: $" + player.getBalance());
            String input = prompt("Seat " + seat.getId() + " — Place your bet (or 'skip' / 'quit'): ");

            if (input.equalsIgnoreCase("quit")) return -1;
            if (input.equalsIgnoreCase("skip")) {
                print("Sitting out this round.");
                return 0;
            }

            try {
                int amount = Integer.parseInt(input.trim());
                // validate early so we can re-prompt on bad bets
                if (amount < seat.getConfig().getMinimumBet()) {
                    print("Bet below table minimum ($" + seat.getConfig().getMinimumBet() + "). Try again.");
                    continue;
                }
                if (amount > seat.getConfig().getMaximumBet()) {
                    print("Bet above table maximum ($" + seat.getConfig().getMaximumBet() + "). Try again.");
                    continue;
                }
                if (amount > player.getBalance()) {
                    print("Insufficient balance ($" + player.getBalance() + "). Try again.");
                    continue;
                }
                if (amount % 2 != 0) {
                    print("Bet must be even (for split payouts). Try again.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                print("Invalid input — enter a number, 'skip', or 'quit'.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // ActionProvider
    // -------------------------------------------------------------------------

    private PlayerAction getAction(Seat seat, Hand hand) {
        print("\nDealer shows: " + table.getDealerUpCard());
        printHand("Your hand", hand);

        List<String> available = buildAvailableActions(hand, seat);
        print("Available actions: " + String.join(", ", available));

        while (true) {
            String input = prompt("Your action: ").trim().toLowerCase();
            switch (input) {
                case "hit", "h"         -> { return PlayerAction.HIT; }
                case "stand", "s"       -> { return PlayerAction.STAND; }
                case "double", "d"      -> {
                    if (!hand.canDoubleDown()) {
                        print("Cannot double down — only allowed on first two cards.");
                    } else if (seat.getPlayer().getBalance() < seat.getBet()) {
                        print("Insufficient balance to double down (need $" + seat.getBet() + ").");
                    } else {
                        return PlayerAction.DOUBLE_DOWN;
                    }
                }
                case "split", "sp"      -> {
                    if (!hand.canSplit()) {
                        print("Cannot split — cards must be a pair.");
                    } else if (seat.getPlayer().getBalance() < seat.getBet()) {
                        print("Insufficient balance to split (need $" + seat.getBet() + ").");
                    } else {
                        return PlayerAction.SPLIT;
                    }
                }
                case "surrender", "sur" -> {
                    if (!hand.canSurrender()) {
                        print("Cannot surrender — only allowed on first two cards.");
                    } else {
                        return PlayerAction.SURRENDER;
                    }
                }
                default -> print("Unknown action. Try: " + String.join(", ", available));
            }
        }
    }

    private List<String> buildAvailableActions(Hand hand, Seat seat) {
        List<String> actions = new ArrayList<>(List.of("hit (h)", "stand (s)"));
        if (hand.canDoubleDown() && seat.getPlayer().getBalance() >= seat.getBet())
            actions.add("double (d)");
        if (hand.canSplit() && seat.getPlayer().getBalance() >= seat.getBet())
            actions.add("split (sp)");
        if (hand.canSurrender())
            actions.add("surrender (sur)");
        return actions;
    }

    // -------------------------------------------------------------------------
    // RoundResultListener
    // -------------------------------------------------------------------------

    private void onRoundComplete(Map<Seat, RoundOutcome> outcomes) {
        print("\n--- Dealer reveals hole card ---");
        printDealerHand();
        print("\n--- Round Result ---");
        outcomes.forEach((seat, outcome) -> {
            Player player = seat.getPlayer();
            String result = switch (outcome) {
                case WIN       -> "WIN  🎉 +$" + seat.getBet();
                case BLACKJACK -> "BLACKJACK! 🃏 +$" + (seat.getBet() * 3 / 2);
                case PUSH      -> "PUSH — bet returned";
                case LOSE      -> "LOSE 💸 -$" + seat.getBet();
                case SURRENDER -> "SURRENDER — half bet returned";
            };
            print("Seat " + seat.getId() + " (" + player.getUsername() + "): " + result);
            print("Balance: $" + player.getBalance());
        });

        print("\nPress Enter to continue or type 'quit' to leave...");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("quit")) {
            // signal game to stop on next bet collection
            // Game will stop naturally when BetProvider returns empty
            // We set a flag here via a small trick — just return quit on next getBets call
            quitRequested = true;
        }
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    private void printWelcome() {
        print("============================================================");
        print("              BLACKJACK — Welcome to the table              ");
        print("============================================================");
    }

    private void printHand(String label, Hand hand) {
        print(label + ": " + hand.getCards() + " (value: " + hand.getValue() + ")");
    }

    private void printDealerHand() {
        Hand dealerHand = table.getDealer().getHand();
        print("Dealer's hand: " + dealerHand.getCards() + " (value: " + dealerHand.getValue() + ")");
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------

    private boolean quitRequested = false;

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private int promptInt(String message, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(prompt(message).trim());
                if (value >= min && value <= max) return value;
                print("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                print("Invalid input — please enter a number.");
            }
        }
    }

    private void print(String message) {
        System.out.println(message);
    }
}