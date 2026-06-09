package com.aditya.blackjack.api.controller;

import com.aditya.blackjack.api.dto.CreateUserRequest;
import com.aditya.blackjack.persistence.entity.HandHistory;
import com.aditya.blackjack.persistence.entity.UserAccount;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Scaffolding — wire up once services and repositories are connected.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<UserAccount> createUser(@RequestBody CreateUserRequest request) {
        // TODO: delegate to UserService
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserAccount> getUser(@PathVariable Long userId) {
        // TODO: delegate to UserService
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<HandHistory>> getHandHistory(@PathVariable Long userId) {
        // TODO: delegate to HandHistoryService
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
