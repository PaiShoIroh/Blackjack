package com.aditya.blackjack.cli;

import com.aditya.blackjack.domain.hand.Hand;
import com.aditya.blackjack.domain.player.Player;
import com.aditya.blackjack.domain.player.PlayerAction;
import com.aditya.blackjack.domain.seat.Seat;
import com.aditya.blackjack.domain.table.Table;
import com.aditya.blackjack.domain.table.TableConfig;
import com.aditya.blackjack.engine.*;

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


        game.addPlayer(player, seatId);

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
            print("Balance: $" + player.getBalance());

            String input = prompt("Seat " + seat.getId() + " — Place your bet (or 'skip' / 'quit'): ");

            if (input.equalsIgnoreCase("quit")) {
                print("Leaving the table...");
                return Map.of(); // empty map signals Game to stop
            }

            if (input.equalsIgnoreCase("skip")) {
                print("Sitting out this round.");
                continue;
            }

            try {
                int amount = Integer.parseInt(input.trim());
                bets.put(seat, amount);
            } catch (NumberFormatException e) {
                print("Invalid bet — sitting out this round.");
            }
        }

        return bets;
    }

    // -------------------------------------------------------------------------
    // ActionProvider
    // -------------------------------------------------------------------------

    private PlayerAction getAction(Seat seat, Hand hand) {
        Hand dealerHand = table.getDealer().getHand();
        print("\nDealer shows: " + table.getDealerUpCard());
        printHand("Your hand", hand);

        List<String> available = buildAvailableActions(hand);
        print("Available actions: " + String.join(", ", available));

        while (true) {
            String input = prompt("Your action: ").trim().toLowerCase();
            switch (input) {
                case "hit", "h"         -> { return PlayerAction.HIT; }
                case "stand", "s"       -> { return PlayerAction.STAND; }
                case "double", "d"      -> {
                    if (hand.canDoubleDown()) return PlayerAction.DOUBLE_DOWN;
                    print("Cannot double down — choose another action.");
                }
                case "split", "sp"      -> {
                    if (hand.canSplit()) return PlayerAction.SPLIT;
                    print("Cannot split — choose another action.");
                }
                case "surrender", "sur" -> {
                    if (hand.canSurrender()) return PlayerAction.SURRENDER;
                    print("Cannot surrender — choose another action.");
                }
                default -> print("Unknown action. Try: " + String.join(", ", available));
            }
        }
    }

    private List<String> buildAvailableActions(Hand hand) {
        List<String> actions = new ArrayList<>(List.of("hit (h)", "stand (s)"));
        if (hand.canDoubleDown()) actions.add("double (d)");
        if (hand.canSplit())      actions.add("split (sp)");
        if (hand.canSurrender())  actions.add("surrender (sur)");
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