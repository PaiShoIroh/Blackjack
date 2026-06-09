package com.aditya.blackjack.exception;

import com.aditya.blackjack.engine.RoundPhase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void gameExceptionWithMessage() {
        GameException ex = new GameException("test error");
        assertThat(ex).hasMessage("test error");
    }

    @Test
    void gameExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root cause");
        GameException ex = new GameException("wrapped", cause);
        assertThat(ex).hasMessage("wrapped").hasCause(cause);
    }

    @Test
    void invalidBetExceptionIsGameException() {
        InvalidBetException ex = new InvalidBetException("bad bet");
        assertThat(ex).isInstanceOf(GameException.class).hasMessage("bad bet");
    }

    @Test
    void invalidActionExceptionIsGameException() {
        InvalidActionException ex = new InvalidActionException("bad action");
        assertThat(ex).isInstanceOf(GameException.class).hasMessage("bad action");
    }

    @Test
    void insufficientBalanceExceptionIsGameException() {
        InsufficientBalanceException ex = new InsufficientBalanceException("no money");
        assertThat(ex).isInstanceOf(GameException.class).hasMessage("no money");
    }

    @Test
    void invalidPhaseExceptionContainsBothPhases() {
        InvalidPhaseException ex = new InvalidPhaseException(RoundPhase.DEALING, RoundPhase.BETTING);
        assertThat(ex).isInstanceOf(GameException.class);
        assertThat(ex.getMessage()).contains("DEALING").contains("BETTING");
    }
}
