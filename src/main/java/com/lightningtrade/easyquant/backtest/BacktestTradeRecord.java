package com.lightningtrade.easyquant.backtest;

import lombok.Data;

@Data
public class BacktestTradeRecord {
    private String symbol;
    private String type; // BUY or SELL
    private double price;
    private int quantity;
    private double profit;
    private String time;
}