package com.lightningtrade.easyquant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacktestTradeRecord {
    private String symbol;
    private LocalDateTime time;
    private String type;
    private double price;
    private int quantity;
    private double profit;
}