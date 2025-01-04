package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.strategy.TradingStrategy;
import org.springframework.stereotype.Component;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import java.util.*;

@Component
public abstract class AbstractBacktestEngine {

    public abstract BacktestResult runBacktest(String symbol, List<Map<String, Object>> historicalData,
            TradingStrategy strategy, double initialCapital, KType kType);

    protected BacktestResult createEmptyResult(String symbol, double initialCapital, KType kType) {
        BacktestResult result = new BacktestResult();
        result.setSymbol(symbol);
        result.setKType(kType);
        result.setInitialCapital(initialCapital);
        result.setFinalCapital(initialCapital);
        result.setTotalReturn(0);
        result.setMaxDrawdown(0);
        result.setSharpeRatio(0);
        result.setWinRate(0);
        result.setTrades(Collections.emptyList());
        result.setEquityCurve(Collections.singletonList(initialCapital));
        return result;
    }

    protected void calculateStatistics(BacktestResult result) {
        List<BacktestTradeRecord> trades = result.getTrades();
        if (trades == null || trades.isEmpty()) {
            return;
        }

        int totalTrades = trades.size();
        int winningTrades = 0;
        double totalProfit = 0;

        for (BacktestTradeRecord trade : trades) {
            if (trade.getProfit() > 0) {
                winningTrades++;
            }
            totalProfit += trade.getProfit();
        }

        // 计算胜率
        double winRate = (double) winningTrades / totalTrades;
        result.setWinRate(winRate);

        // 计算夏普比率
        if (result.getEquityCurve() != null && result.getEquityCurve().size() > 1) {
            double sharpeRatio = calculateSharpeRatio(result.getEquityCurve(), result.getKType());
            result.setSharpeRatio(sharpeRatio);
        }
    }

    protected double calculateSharpeRatio(List<Double> equityCurve, KType kType) {
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < equityCurve.size(); i++) {
            double returnRate = (equityCurve.get(i) - equityCurve.get(i - 1)) / equityCurve.get(i - 1);
            returns.add(returnRate);
        }

        // 计算平均收益率和标准差
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double stdDev = Math.sqrt(returns.stream()
                .mapToDouble(r -> Math.pow(r - avgReturn, 2))
                .average()
                .orElse(0));

        // 根据K线周期调整年化因子
        int annualizationFactor = getAnnualizationFactor(kType);

        // 假设无风险利率为3%
        double riskFreeRate = 0.03 / annualizationFactor; // 转换为对应周期利率
        return stdDev == 0 ? 0 : (avgReturn - riskFreeRate) / stdDev * Math.sqrt(annualizationFactor);
    }

    protected int getAnnualizationFactor(KType kType) {
        // 根据K线周期返回年化因子
        switch (kType) {
            case min1:
                return 252 * 390; // 一年约252个交易日，每天390分钟
            case min5:
                return 252 * 78; // 390/5
            case min15:
                return 252 * 26; // 390/15
            case min30:
                return 252 * 13; // 390/30
            case min60:
                return (int) (252 * 6.5); // 390/60
            case day:
                return 252; // 一年约252个交易日
            case week:
                return 52; // 一年52周
            case month:
                return 12; // 一年12个月
            case year:
                return 1;
            case min120:
                return 252 * 2; // 390/120

            default:
                return 252; // 默认使用日线的年化因子
        }
    }

    protected double calculateMaxDrawdown(List<Double> equityCurve) {
        if (equityCurve == null || equityCurve.isEmpty()) {
            return 0;
        }

        double maxDrawdown = 0;
        double peak = equityCurve.get(0);

        for (double value : equityCurve) {
            if (value > peak) {
                peak = value;
            }
            double drawdown = (peak - value) / peak;
            maxDrawdown = Math.max(maxDrawdown, drawdown);
        }

        return maxDrawdown;
    }
}