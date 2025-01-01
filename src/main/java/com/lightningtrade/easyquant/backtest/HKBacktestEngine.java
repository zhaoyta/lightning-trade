package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.strategy.TradingStrategy;
import java.util.Map;

/**
 * 港股回测引擎
 */
public class HKBacktestEngine extends AbstractBacktestEngine {

    public HKBacktestEngine(Map<String, TradingStrategy> strategies, double initialCapital) {
        super(strategies, initialCapital);
    }

    @Override
    protected int getLotSize(String symbol) {
        // 港股不同股票的每手股数不同，这里需要根据实际情况设置
        // 可以通过配置文件或数据库来存储不同股票的每手股数
        return 100; // 默认每手100股
    }

    @Override
    protected double getTransactionCost(String symbol, int quantity, double price) {
        double totalAmount = price * quantity;

        // 港股交易成本计算
        // 1. 佣金：成交金额的0.25%，最低100港币
        double commission = Math.max(totalAmount * 0.0025, 100);

        // 2. 印花税：成交金额的0.13%
        double stampDuty = totalAmount * 0.0013;

        // 3. 交易征费：成交金额的0.00015%
        double tradingFee = totalAmount * 0.0000015;

        // 4. 交易系统使用费：每笔交易0.5港币
        double systemUsageFee = 0.5;

        // 5. 交易费：成交金额的0.00005%
        double tradingTariff = totalAmount * 0.0000005;

        return commission + stampDuty + tradingFee + systemUsageFee + tradingTariff;
    }
}