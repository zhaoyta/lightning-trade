package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.strategy.TradingStrategy;
import org.springframework.stereotype.Component;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import java.util.*;

@Component
public class BacktestEngine extends AbstractBacktestEngine {

    @Override
    public BacktestResult runBacktest(String symbol, List<Map<String, Object>> historicalData,
            TradingStrategy strategy, double initialCapital, KType kType) {
        if (historicalData == null || historicalData.isEmpty()) {
            return createEmptyResult(symbol, initialCapital, kType);
        }

        BacktestResult result = strategy.backtest(symbol, historicalData, initialCapital, kType);
        result.setKType(kType); // 设置K线周期

        // 计算其他统计指标
        if (result.getTrades() != null && !result.getTrades().isEmpty()) {
            calculateStatistics(result);
        }

        return result;
    }
}