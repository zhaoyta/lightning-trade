package com.lightningtrade.easyquant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshot {
    private double cash;
    private double stockValue;
    private double totalValue;
    private Map<String, Position> positions;

    public PortfolioSnapshot(double cash, double stockValue, double totalValue) {
        this.cash = cash;
        this.stockValue = stockValue;
        this.totalValue = totalValue;
    }
}