package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import com.lightningtrade.easyquant.model.MarketData;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 布林带策略实现类
 * 布林带是一种利用统计学原理的技术分析指标，由中轨（移动平均线）和上下轨（标准差通道）组成
 * 计算过程：
 * 1. 计算N日移动平均线（MA）作为中轨
 * 2. 计算N日价格标准差（Standard Deviation）
 * 3. 上轨 = MA + K * StdDev
 * 4. 下轨 = MA - K * StdDev
 * 交易信号：
 * - 当价格突破下轨时，表示超卖，产生买入信号
 * - 当价格突破上轨时，表示超买，产生卖出信号
 * K值通常取2，这样布林带可以包含约95%的价格波动
 */
@Component
public class BollingerBandsStrategy extends AbstractTradingStrategy {
    // 移动平均线周期
    private final int period;
    // 标准差倍数，用于计算上下轨道的宽度
    private final double stdDevMultiplier;
    // 存储价格队列，用于计算移动平均和标准差
    private final Queue<Double> prices = new LinkedList<>();
    // 价格总和，用于计算移动平均线
    private double sum = 0;
    // 价格平方和，用于计算标准差
    private double sumSquares = 0;

    /**
     * 默认构造函数，使用标准的布林带参数设置
     * period=20: 20日移动平均线
     * stdDevMultiplier=2.0: 2倍标准差（约95%置信区间）
     */
    public BollingerBandsStrategy() {
        this(20, 2.0); // 默认使用20日布林带，2倍标准差
    }

    /**
     * 自定义参数构造函数
     * 
     * @param period           移动平均线周期
     * @param stdDevMultiplier 标准差倍数，用于控制带宽
     */
    public BollingerBandsStrategy(int period, double stdDevMultiplier) {
        this.period = period;
        this.stdDevMultiplier = stdDevMultiplier;
    }

    /**
     * 计算交易信号
     * 
     * @param data 市场数据
     * @return 交易信号：BUY（买入）, SELL（卖出）, null（无信号）
     */
    @Override
    protected String calculateSignal(MarketData data) {
        double price = data.getClose();

        // 更新价格队列和统计数据
        // 将新价格加入队列并更新总和与平方和
        prices.offer(price);
        sum += price;
        sumSquares += price * price;

        // 当队列长度超过周期时，移除最早的数据并更新统计值
        if (prices.size() > period) {
            double oldPrice = prices.poll();
            sum -= oldPrice;
            sumSquares -= oldPrice * oldPrice;
        }

        // 等待数据量达到要求的周期数
        if (prices.size() < period) {
            return null;
        }

        // 计算布林带
        // 计算移动平均线（中轨）
        double ma = sum / period;
        // 计算方差：E(X^2) - (E(X))^2
        double variance = (sumSquares - (sum * sum / period)) / (period - 1);
        // 计算标准差
        double stdDev = Math.sqrt(variance);
        // 计算上轨：MA + K * StdDev
        double upperBand = ma + stdDevMultiplier * stdDev;
        // 计算下轨：MA - K * StdDev
        double lowerBand = ma - stdDevMultiplier * stdDev;

        // 生成交易信号
        // 价格跌破下轨，表示超卖，产生买入信号
        if (price < lowerBand) {
            return "BUY";
        }
        // 价格突破上轨，表示超买，产生卖出信号
        else if (price > upperBand) {
            return "SELL";
        }

        return null;
    }
}