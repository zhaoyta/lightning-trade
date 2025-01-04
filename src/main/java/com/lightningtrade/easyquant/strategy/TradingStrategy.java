package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.model.MarketData;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import java.util.List;

public interface TradingStrategy {
    BacktestResult backtest(String symbol, List<MarketData> data, double initialCapital, KType kType);
}