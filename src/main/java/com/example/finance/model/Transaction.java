package com.example.finance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerShare;

    @Column(nullable = false)
    private LocalDateTime transactionTimestamp;

    public enum TransactionType {
        BUY, SELL
    }
}