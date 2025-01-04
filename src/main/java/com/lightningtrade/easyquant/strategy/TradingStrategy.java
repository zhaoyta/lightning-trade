package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import java.util.List;
import java.util.Map;

public interface TradingStrategy {
    BacktestResult backtest(String symbol, List<Map<String, Object>> data, double initialCapital, KType kType);
}