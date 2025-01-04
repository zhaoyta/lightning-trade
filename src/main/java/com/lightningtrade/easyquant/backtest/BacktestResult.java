package com.lightningtrade.easyquant.backtest;

import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import lombok.Data;
import java.util.List;

@Data
public class BacktestResult {
    private String symbol;
    private KType kType;
    private double initialCapital;
    private double finalCapital;
    private double totalReturn;
    private double maxDrawdown;
    private double sharpeRatio;
    private double winRate;
    private List<BacktestTradeRecord> trades;
    private List<Double> equityCurve;
}