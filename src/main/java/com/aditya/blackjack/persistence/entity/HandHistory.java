package com.aditya.blackjack.persistence.entity;

import com.aditya.blackjack.engine.RoundOutcome;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "hand_histories")
@Getter
@Setter
@NoArgsConstructor
public class HandHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private int betAmount;

    @Column(nullable = false)
    private String playerCards;

    @Column(nullable = false)
    private String dealerCards;

    @Column(nullable = false)
    private int playerFinalValue;

    @Column(nullable = false)
    private int dealerFinalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundOutcome outcome;

    @Column(nullable = false)
    private int payout;

    @Column(nullable = false)
    private int balanceAfter;

    @Column(nullable = false, updatable = false)
    private Instant playedAt;

    @PrePersist
    void onCreate() {
        playedAt = Instant.now();
    }
}
