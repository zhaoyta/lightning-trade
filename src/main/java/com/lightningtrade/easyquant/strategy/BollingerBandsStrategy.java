package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
public class BollingerBandsStrategy extends AbstractTradingStrategy {
    private final int period;
    private final double stdDevMultiplier;
    private final Queue<Double> prices = new LinkedList<>();
    private double sum = 0;
    private double sumSquares = 0;

    public BollingerBandsStrategy() {
        this(20, 2.0); // 默认使用20日布林带，2倍标准差
    }

    public BollingerBandsStrategy(int period, double stdDevMultiplier) {
        this.period = period;
        this.stdDevMultiplier = stdDevMultiplier;
    }

    @Override
    protected String calculateSignal(Map<String, Object> data) {
        double price = (double) data.get("close");

        // 更新价格队列和统计数据
        prices.offer(price);
        sum += price;
        sumSquares += price * price;

        if (prices.size() > period) {
            double oldPrice = prices.poll();
            sum -= oldPrice;
            sumSquares -= oldPrice * oldPrice;
        }

        if (prices.size() < period) {
            return null;
        }

        // 计算布林带
        double ma = sum / period;
        double variance = (sumSquares - (sum * sum / period)) / (period - 1);
        double stdDev = Math.sqrt(variance);
        double upperBand = ma + stdDevMultiplier * stdDev;
        double lowerBand = ma - stdDevMultiplier * stdDev;

        // 生成交易信号
        if (price < lowerBand) {
            return "BUY";
        } else if (price > upperBand) {
            return "SELL";
        }

        return null;
    }
}