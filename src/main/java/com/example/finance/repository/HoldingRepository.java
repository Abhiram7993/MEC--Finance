package com.example.finance.repository;

import com.example.finance.model.Holding;
import com.example.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByUserAndSymbol(User user, String symbol);
    Set<Holding> findByUserOrderBySymbol(User user);
}