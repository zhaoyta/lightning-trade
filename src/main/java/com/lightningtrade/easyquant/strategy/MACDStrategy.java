package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import com.lightningtrade.easyquant.model.MarketData;

/**
 * MACD (Moving Average Convergence Divergence) 策略实现类
 * MACD是一种趋势跟踪动量指标，通过比较两条不同速度的移动平均线来判断买卖时机
 * 计算过程：
 * 1. 计算快速EMA和慢速EMA
 * 2. MACD线 = 快速EMA - 慢速EMA
 * 3. 信号线 = MACD的移动平均线
 * - 当MACD线从下向上穿过信号线时产生买入信号（金叉）
 * - 当MACD线从上向下穿过信号线时产生卖出信号（死叉）
 */
@Component
public class MACDStrategy extends AbstractTradingStrategy {
    // 快速移动平均线周期
    private final int fastPeriod;
    // 慢速移动平均线周期
    private final int slowPeriod;
    // 信号线周期
    private final int signalPeriod;
    // 快速指数移动平均线的当前值
    private double fastEMA = 0;
    // 慢速指数移动平均线的当前值
    private double slowEMA = 0;
    // 信号线的当前值（MACD的移动平均线）
    private double signalEMA = 0;
    // 上一次的MACD值
    private Double lastMACD = null;
    // 上一次的信号线值
    private Double lastSignal = null;
    // 指标是否已经完成初始化
    private boolean initialized = false;
    // 接收到的价格点计数
    private int count = 0;

    /**
     * 默认构造函数，使用标准的MACD参数设置
     * fastPeriod=12: 12日快速移动平均线
     * slowPeriod=26: 26日慢速移动平均线
     * signalPeriod=9: 9日信号线
     */
    public MACDStrategy() {
        this(12, 26, 9); // 默认使用12、26、9
    }

    /**
     * 自定义参数构造函数
     * 
     * @param fastPeriod   快速移动平均线周期
     * @param slowPeriod   慢速移动平均线周期
     * @param signalPeriod 信号线周期
     */
    public MACDStrategy(int fastPeriod, int slowPeriod, int signalPeriod) {
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
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
        count++;

        // 初始化EMA值
        // EMA需要一定数量的数据才能开始产生有效的信号
        // 使用第一个价格点作为EMA的初始值
        if (!initialized) {
            if (count == 1) {
                fastEMA = price;
                slowEMA = price;
                signalEMA = 0;
            } else if (count == slowPeriod) {
                initialized = true;
            }
            return null;
        }

        // 计算MACD指标
        // alpha = 2/(period + 1) 是EMA的平滑系数
        double fastAlpha = 2.0 / (fastPeriod + 1);
        double slowAlpha = 2.0 / (slowPeriod + 1);
        double signalAlpha = 2.0 / (signalPeriod + 1);

        // 计算快速和慢速EMA
        // EMA = 当前价格 * alpha + 前一日EMA * (1 - alpha)
        fastEMA = price * fastAlpha + fastEMA * (1 - fastAlpha);
        slowEMA = price * slowAlpha + slowEMA * (1 - slowAlpha);

        // 计算MACD线和信号线
        double macd = fastEMA - slowEMA;
        signalEMA = macd * signalAlpha + signalEMA * (1 - signalAlpha);

        String signal = null;
        if (lastMACD != null && lastSignal != null) {
            // MACD金叉：MACD线从下穿过信号线，产生买入信号
            if (lastMACD <= lastSignal && macd > signalEMA) {
                signal = "BUY";
            }
            // MACD死叉：MACD线从上穿过信号线，产生卖出信号
            else if (lastMACD >= lastSignal && macd < signalEMA) {
                signal = "SELL";
            }
        }

        // 保存当前值用于下次计算
        lastMACD = macd;
        lastSignal = signalEMA;

        return signal;
    }
}