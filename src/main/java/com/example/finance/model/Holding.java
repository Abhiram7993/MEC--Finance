package com.example.finance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "symbol"})
})
@Getter
@Setter
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    @Transient // This field is not stored in the database
    private BigDecimal currentPrice;

    @Transient // This field is not stored in the database
    public BigDecimal getTotalValue() {
        if (currentPrice == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.multiply(new BigDecimal(shares));
    }
}