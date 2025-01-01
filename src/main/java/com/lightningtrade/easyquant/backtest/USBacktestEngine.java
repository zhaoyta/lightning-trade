package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.strategy.TradingStrategy;
import java.util.Map;

/**
 * 美股回测引擎
 */
public class USBacktestEngine extends AbstractBacktestEngine {

    public USBacktestEngine(Map<String, TradingStrategy> strategies, double initialCapital) {
        super(strategies, initialCapital);
    }

    @Override
    protected int getLotSize(String symbol) {
        return 100; // 美股最小交易单位是100股
    }

    @Override
    protected double getTransactionCost(String symbol, int quantity, double price) {
        // 美股交易成本计算
        // 1. 佣金：每股0.0049美元，最低2.99美元
        double commission = Math.max(quantity * 0.0049, 2.99);
        // 2. SEC费用：卖出时收取，成交金额的0.0008%
        double secFee = price * quantity * 0.000008;
        // 3. TAF费用：每笔交易0.02美元
        double tafFee = 0.02;
        // 4. 清算费：每笔交易0.02美元
        double clearingFee = 0.02;

        return commission + secFee + tafFee + clearingFee;
    }
}