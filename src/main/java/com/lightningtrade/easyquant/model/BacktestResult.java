package com.lightningtrade.easyquant.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class BacktestResult {
    private String strategy;
    private List<String> symbols;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double initialCapital;
    private double finalCapital;
    private double totalReturn;
    private double maxDrawdown;
    private int totalTrades;
    private int winningTrades;
    private double winRate;
    private double sharpeRatio;
    private List<BacktestTradeRecord> trades;
    private Map<LocalDateTime, PortfolioSnapshot> portfolioHistory;
}