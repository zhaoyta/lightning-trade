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

    // 交易费率（假设为0.1%）
    protected static final double TRANSACTION_FEE_RATE = 0.001;

    // 最小交易单位
    protected static final int MIN_TRADE_UNIT = 100;

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
        // 当前持仓数量
        int currentPosition = 0;
        // 最大资金
        double maxCapital = initialCapital;
        // 最大回撤
        double maxDrawdown = 0;

        // 遍历每一根K线
        for (MarketData bar : data) {
            // 计算策略信号
            String signal = calculateSignal(bar);

            // 如果有交易信号，检查是否可以执行交易
            if (signal != null) {
                int tradeQuantity = calculateTradeQuantity(signal, currentCapital, currentPosition, bar.getClose());

                if (tradeQuantity > 0) {
                    // 创建交易记录
                    BacktestTradeRecord trade = new BacktestTradeRecord();
                    trade.setSymbol(symbol);
                    trade.setType(signal);
                    trade.setPrice(bar.getClose());
                    trade.setTime(bar.getDateTime());
                    trade.setQuantity(tradeQuantity);

                    // 更新资金和持仓
                    double tradeCost = calculateTradeCost(tradeQuantity, bar.getClose());
                    if ("BUY".equals(signal)) {
                        currentCapital -= (bar.getClose() * tradeQuantity + tradeCost);
                        currentPosition += tradeQuantity;
                        trade.setProfit(0);
                    } else if ("SELL".equals(signal)) {
                        double profit = bar.getClose() * tradeQuantity - tradeCost;
                        currentCapital += profit;
                        currentPosition -= tradeQuantity;
                        trade.setProfit(profit);
                    }

                    trades.add(trade);
                }
            }

            // 更新权益曲线（包括持仓市值）
            double totalEquity = currentCapital + (currentPosition * bar.getClose());
            equityCurve.add(totalEquity);

            // 计算最大回撤
            maxCapital = Math.max(maxCapital, totalEquity);
            double drawdown = (maxCapital - totalEquity) / maxCapital;
            maxDrawdown = Math.max(maxDrawdown, drawdown);
        }

        // 设置回测结果
        double finalEquity = currentCapital + (currentPosition * data.get(data.size() - 1).getClose());
        result.setFinalCapital(finalEquity);
        result.setTotalReturn((finalEquity - initialCapital) / initialCapital);
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
     * 计算可交易数量
     * 
     * @param signal   交易信号
     * @param capital  当前资金
     * @param position 当前持仓
     * @param price    当前价格
     * @return 可交易数量
     */
    protected int calculateTradeQuantity(String signal, double capital, int position, double price) {
        if ("BUY".equals(signal)) {
            // 计算最大可买数量（考虑交易费用）
            double maxAmount = capital / (price * (1 + TRANSACTION_FEE_RATE));
            int maxQuantity = (int) (maxAmount / MIN_TRADE_UNIT) * MIN_TRADE_UNIT;
            return maxQuantity > 0 ? maxQuantity : 0;
        } else if ("SELL".equals(signal)) {
            // 返回当前持仓数量（如果有持仓的话）
            return position > 0 ? position : 0;
        }
        return 0;
    }

    /**
     * 计算交易成本
     * 
     * @param quantity 交易数量
     * @param price    交易价格
     * @return 交易成本
     */
    protected double calculateTradeCost(int quantity, double price) {
        return quantity * price * TRANSACTION_FEE_RATE;
    }
}