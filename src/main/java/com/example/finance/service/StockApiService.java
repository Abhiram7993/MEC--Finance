package com.example.finance.service;

import com.example.finance.dto.StockQuote;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StockApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://finance.cs50.io/quote?symbol=";

    public StockQuote lookup(String symbol) {
        try {
            String url = API_URL + symbol.toUpperCase();
            StockQuote quote = restTemplate.getForObject(url, StockQuote.class);
            if (quote != null && quote.getSymbol() != null) {
                return quote;
            }
            return null;
        } catch (Exception e) {
            // Log the error or handle it as needed
            return null;
        }
    }
}