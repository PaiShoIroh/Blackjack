package com.aditya.blackjack.domain.player;

import com.aditya.blackjack.exception.GameException;
import com.aditya.blackjack.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerTest {

    @Test
    void playerInitialisesWithUsernameAndBalance() {
        Player player = new Player("aditya", 1000);
        assertThat(player.getUsername()).isEqualTo("aditya");
        assertThat(player.getBalance()).isEqualTo(1000);
    }

    @Test
    void debitReducesBalance() {
        Player player = new Player("aditya", 1000);
        player.debit(200);
        assertThat(player.getBalance()).isEqualTo(800);
    }

    @Test
    void creditIncreasesBalance() {
        Player player = new Player("aditya", 1000);
        player.credit(500);
        assertThat(player.getBalance()).isEqualTo(1500);
    }

    @Test
    void debitExactBalanceLeavesZero() {
        Player player = new Player("aditya", 500);
        player.debit(500);
        assertThat(player.getBalance()).isEqualTo(0);
    }

    @Test
    void debitMoreThanBalanceThrows() {
        Player player = new Player("aditya", 100);
        assertThatThrownBy(() -> player.debit(200))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient");
    }

    @Test
    void negativeDebitThrows() {
        Player player = new Player("aditya", 1000);
        assertThatThrownBy(() -> player.debit(-50))
                .isInstanceOf(GameException.class);
    }

    @Test
    void negativeCreditThrows() {
        Player player = new Player("aditya", 1000);
        assertThatThrownBy(() -> player.credit(-50))
                .isInstanceOf(GameException.class);
    }

    @Test
    void negativeInitialBalanceThrows() {
        assertThatThrownBy(() -> new Player("aditya", -100))
                .isInstanceOf(GameException.class);
    }
}