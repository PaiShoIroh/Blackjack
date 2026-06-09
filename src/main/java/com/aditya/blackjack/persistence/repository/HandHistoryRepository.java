package com.aditya.blackjack.persistence.repository;

import com.aditya.blackjack.persistence.entity.HandHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandHistoryRepository extends JpaRepository<HandHistory, Long> {
    List<HandHistory> findByUserIdOrderByPlayedAtDesc(Long userId);
}
