package com.aditya.blackjack.api;

import com.aditya.blackjack.exception.GameException;
import com.aditya.blackjack.exception.InsufficientBalanceException;
import com.aditya.blackjack.exception.InvalidActionException;
import com.aditya.blackjack.exception.InvalidBetException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidBetException.class)
    public ResponseEntity<Map<String, String>> handleInvalidBet(InvalidBetException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAction(InvalidActionException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientBalance(InsufficientBalanceException e) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(GameException.class)
    public ResponseEntity<Map<String, String>> handleGameException(GameException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("error", e.getMessage()));
    }
}
