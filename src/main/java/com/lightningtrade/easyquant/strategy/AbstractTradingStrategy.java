package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.backtest.BacktestTradeRecord;
import com.lightningtrade.easyquant.model.MarketData;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象交易策略类
 * 这是所有交易策略的基类，提供了回测功能的基本实现
 */
public abstract class AbstractTradingStrategy implements TradingStrategy {

    // K线类型（比如1分钟、5分钟、日K等）
    protected KType kType;

    /**
     * 执行回测的方法
     * 
     * @param symbol         交易品种的代码
     * @param data           历史数据列表
     * @param initialCapital 初始资金
     * @param kType          K线类型
     * @return 回测结果
     */
    @Override
    public BacktestResult backtest(String symbol, List<MarketData> data, double initialCapital, KType kType) {
        this.kType = kType;
        BacktestResult result = new BacktestResult();
        result.setSymbol(symbol);
        result.setInitialCapital(initialCapital);
        result.setKType(kType);

        // 用于记录所有交易
        List<BacktestTradeRecord> trades = new ArrayList<>();
        // 记录权益曲线
        List<Double> equityCurve = new ArrayList<>();
        // 当前资金
        double currentCapital = initialCapital;
        // 最大资金
        double maxCapital = initialCapital;
        // 最大回撤
        double maxDrawdown = 0;

        // 遍历每一根K线
        for (MarketData bar : data) {
            // 计算策略信号
            String signal = calculateSignal(bar);

            // 如果有交易信号，记录交易
            if (signal != null) {
                BacktestTradeRecord trade = new BacktestTradeRecord();
                trade.setSymbol(symbol);
                trade.setType(signal);
                trade.setPrice(bar.getClose());
                trade.setTime(bar.getDateTime().toString());
                trades.add(trade);
            }

            // 更新权益曲线
            currentCapital = updateCapital(currentCapital, signal, bar.getClose());
            equityCurve.add(currentCapital);

            // 计算最大回撤
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

    /**
     * 计算交易信号的抽象方法，需要由具体策略实现
     * 
     * @param data 当前K线数据
     * @return 交易信号（买入/卖出）
     */
    protected abstract String calculateSignal(MarketData data);

    /**
     * 更新资金的方法
     * 
     * @param capital 当前资金
     * @param signal  交易信号
     * @param price   当前价格
     * @return 更新后的资金
     */
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