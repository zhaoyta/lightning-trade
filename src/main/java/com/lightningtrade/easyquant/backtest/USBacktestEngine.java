package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import org.springframework.stereotype.Component;

import java.util.*;

@Component("usBacktestEngine")
public class USBacktestEngine extends BacktestEngine {

    private static final double COMMISSION_RATE = 0.0025; // 0.25% 佣金率
    private static final int LOT_SIZE = 100; // 美股标准手数为100股

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
            // 更新交易成本
            updateTradingCosts(result);
            // 计算统计指标
            calculateStatistics(result);
        }

        return result;
    }

    private void updateTradingCosts(BacktestResult result) {
        List<BacktestTradeRecord> trades = result.getTrades();
        double currentCapital = result.getInitialCapital();
        List<Double> equityCurve = new ArrayList<>();
        equityCurve.add(currentCapital);

        for (BacktestTradeRecord trade : trades) {
            double tradeValue = trade.getPrice() * trade.getQuantity();
            double commission = tradeValue * COMMISSION_RATE;

            // 更新交易成本
            if ("BUY".equals(trade.getType())) {
                currentCapital -= (tradeValue + commission);
            } else if ("SELL".equals(trade.getType())) {
                currentCapital += (tradeValue - commission);
                // 更新交易利润（扣除佣金）
                trade.setProfit(trade.getProfit() - commission * 2); // 买入和卖出的佣金
            }

            equityCurve.add(currentCapital);
        }

        // 更新最终资金和权益曲线
        result.setFinalCapital(currentCapital);
        result.setEquityCurve(equityCurve);
        result.setTotalReturn((currentCapital - result.getInitialCapital()) / result.getInitialCapital());
    }

    public int getLotSize() {
        return LOT_SIZE;
    }

    public double getCommissionRate() {
        return COMMISSION_RATE;
    }
}