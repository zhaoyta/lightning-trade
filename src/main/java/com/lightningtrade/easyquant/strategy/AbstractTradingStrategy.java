package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.backtest.BacktestTradeRecord;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTradingStrategy implements TradingStrategy {

    protected KType kType;

    @Override
    public BacktestResult backtest(String symbol, List<Map<String, Object>> data, double initialCapital, KType kType) {
        this.kType = kType;
        BacktestResult result = new BacktestResult();
        result.setSymbol(symbol);
        result.setInitialCapital(initialCapital);
        result.setKType(kType);

        List<BacktestTradeRecord> trades = new ArrayList<>();
        List<Double> equityCurve = new ArrayList<>();
        double currentCapital = initialCapital;
        double maxCapital = initialCapital;
        double maxDrawdown = 0;

        for (Map<String, Object> bar : data) {
            // 计算策略信号
            String signal = calculateSignal(bar);

            if (signal != null) {
                BacktestTradeRecord trade = new BacktestTradeRecord();
                trade.setSymbol(symbol);
                trade.setType(signal);
                trade.setPrice((double) bar.get("close"));
                trade.setTime(bar.get("time").toString());
                trades.add(trade);
            }

            // 更新权益曲线
            currentCapital = updateCapital(currentCapital, signal, (double) bar.get("close"));
            equityCurve.add(currentCapital);

            // 更新最大回撤
            maxCapital = Math.max(maxCapital, currentCapital);
            double drawdown = (maxCapital - currentCapital) / maxCapital;
            maxDrawdown = Math.max(maxDrawdown, drawdown);
        }

        // 设置回测结果
        result.setFinalCapital(currentCapital);
        result.setTotalReturn((currentCapital - initialCapital) / initialCapital);
        result.setMaxDrawdown(maxDrawdown);
        result.setTrades(trades);
        result.setEquityCurve(equityCurve);

        return result;
    }

    protected abstract String calculateSignal(Map<String, Object> data);

    private double updateCapital(double capital, String signal, double price) {
        // 简单的资金计算逻辑，实际交易中需要考虑更多因素
        if ("BUY".equals(signal)) {
            return capital * 0.99; // 假设交易成本为1%
        } else if ("SELL".equals(signal)) {
            return capital * 1.01; // 假设盈利1%
        }
        return capital;
    }
}