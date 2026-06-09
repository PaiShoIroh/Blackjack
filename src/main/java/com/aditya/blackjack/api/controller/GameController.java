package com.aditya.blackjack.api.controller;

import com.aditya.blackjack.api.dto.GameStateResponse;
import com.aditya.blackjack.api.dto.PlaceBetRequest;
import com.aditya.blackjack.api.dto.PlayerActionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Scaffolding — wire up once GameSessionService is implemented.
 *
 * The API is session-based: each table has a session ID.
 * Unlike the CLI's synchronous game loop, the API exposes each
 * round phase as a discrete endpoint so the frontend controls pacing.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    @PostMapping
    public ResponseEntity<String> createGame() {
        // TODO: create a new table session, return session ID
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<GameStateResponse> joinTable(
            @PathVariable String sessionId,
            @RequestParam Long userId,
            @RequestParam int seatId) {
        // TODO: seat player at table
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @PostMapping("/{sessionId}/bet")
    public ResponseEntity<GameStateResponse> placeBet(
            @PathVariable String sessionId,
            @RequestBody PlaceBetRequest request) {
        // TODO: place bet, trigger deal if all bets are in
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @PostMapping("/{sessionId}/action")
    public ResponseEntity<GameStateResponse> playerAction(
            @PathVariable String sessionId,
            @RequestBody PlayerActionRequest request) {
        // TODO: execute player action, return updated state
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @GetMapping("/{sessionId}/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable String sessionId) {
        // TODO: return current game state
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
