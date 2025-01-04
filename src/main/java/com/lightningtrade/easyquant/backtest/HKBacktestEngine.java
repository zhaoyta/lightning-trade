package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import org.springframework.stereotype.Component;

import java.util.*;

@Component("hkBacktestEngine")
public class HKBacktestEngine extends BacktestEngine {

    private static final double COMMISSION_RATE = 0.0027; // 0.27% 佣金率
    private static final double STAMP_DUTY = 0.001; // 0.1% 印花税
    private static final int DEFAULT_LOT_SIZE = 100; // 默认每手100股

    private final Map<String, Integer> lotSizeMap = new HashMap<>();

    public HKBacktestEngine() {
        // 初始化一些常见股票的每手股数
        lotSizeMap.put("00700", 100); // 腾讯
        lotSizeMap.put("09988", 100); // 阿里巴巴
        lotSizeMap.put("03690", 100); // 美团
        // 可以添加更多股票的每手股数
    }

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
            double stampDuty = "SELL".equals(trade.getType()) ? tradeValue * STAMP_DUTY : 0;

            // 更新交易成本
            if ("BUY".equals(trade.getType())) {
                currentCapital -= (tradeValue + commission);
            } else if ("SELL".equals(trade.getType())) {
                currentCapital += (tradeValue - commission - stampDuty);
                // 更新交易利润（扣除佣金和印花税）
                trade.setProfit(trade.getProfit() - commission * 2 - stampDuty); // 买入和卖出的佣金，以及卖出的印花税
            }

            equityCurve.add(currentCapital);
        }

        // 更新最终资金和权益曲线
        result.setFinalCapital(currentCapital);
        result.setEquityCurve(equityCurve);
        result.setTotalReturn((currentCapital - result.getInitialCapital()) / result.getInitialCapital());
    }

    public int getLotSize(String symbol) {
        return lotSizeMap.getOrDefault(symbol, DEFAULT_LOT_SIZE);
    }

    public double getCommissionRate() {
        return COMMISSION_RATE;
    }

    public double getStampDuty() {
        return STAMP_DUTY;
    }
}