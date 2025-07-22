package com.example.finance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockQuote {

    @JsonProperty("companyName")
    private String name;

    @JsonProperty("latestPrice")
    private BigDecimal price;

    private String symbol;
}