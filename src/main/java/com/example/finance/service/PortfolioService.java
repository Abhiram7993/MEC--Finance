package com.example.finance.service;

import com.example.finance.dto.StockQuote;
import com.example.finance.model.Holding;
import com.example.finance.model.Transaction;
import com.example.finance.model.User;
import com.example.finance.repository.HoldingRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PortfolioService {

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final StockApiService stockApiService;

    public PortfolioService(UserRepository userRepository, HoldingRepository holdingRepository,
                            TransactionRepository transactionRepository, StockApiService stockApiService) {
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.stockApiService = stockApiService;
    }

    @Transactional
    public void buyStock(User user, String symbol, int shares) throws Exception {
        StockQuote quote = stockApiService.lookup(symbol);
        if (quote == null) {
            throw new Exception("Invalid stock symbol.");
        }
        BigDecimal totalCost = quote.getPrice().multiply(new BigDecimal(shares));
        if (user.getCash().compareTo(totalCost) < 0) {
            throw new Exception("Insufficient funds.");
        }

        // Deduct cash
        user.setCash(user.getCash().subtract(totalCost));

        // Add or update holding
        Holding holding = holdingRepository.findByUserAndSymbol(user, quote.getSymbol())
                .orElse(new Holding());
        holding.setUser(user);
        holding.setSymbol(quote.getSymbol());
        holding.setShares(holding.getShares() + shares);
        holdingRepository.save(holding);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.BUY);
        transaction.setSymbol(quote.getSymbol());
        transaction.setShares(shares);
        transaction.setPricePerShare(quote.getPrice());
        transaction.setTransactionTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
        
        userRepository.save(user);
    }

    @Transactional
    public void sellStock(User user, String symbol, int sharesToSell) throws Exception {
        Holding holding = holdingRepository.findByUserAndSymbol(user, symbol)
                .orElseThrow(() -> new Exception("You do not own shares of this stock."));

        if (sharesToSell > holding.getShares()) {
            throw new Exception("You do not have enough shares to sell.");
        }

        StockQuote quote = stockApiService.lookup(symbol);
        if (quote == null) {
            throw new Exception("Could not retrieve current price for symbol: " + symbol);
        }

        BigDecimal totalSaleValue = quote.getPrice().multiply(new BigDecimal(sharesToSell));

        // Add cash
        user.setCash(user.getCash().add(totalSaleValue));

        // Decrease shares
        holding.setShares(holding.getShares() - sharesToSell);
        if (holding.getShares() == 0) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.SELL);
        transaction.setSymbol(symbol);
        transaction.setShares(sharesToSell);
        transaction.setPricePerShare(quote.getPrice());
        transaction.setTransactionTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
        
        userRepository.save(user);
    }

    public Set<Holding> getUserHoldingsWithCurrentPrices(User user) {
        Set<Holding> holdings = holdingRepository.findByUserOrderBySymbol(user);
        for (Holding holding : holdings) {
            StockQuote quote = stockApiService.lookup(holding.getSymbol());
            if (quote != null) {
                holding.setCurrentPrice(quote.getPrice());
            }
        }
        return holdings;
    }

    public List<Transaction> getTransactionHistory(User user) {
        return transactionRepository.findByUserOrderByTransactionTimestampDesc(user);
    }
}