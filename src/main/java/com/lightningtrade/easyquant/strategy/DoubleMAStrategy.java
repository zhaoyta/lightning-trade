package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import com.lightningtrade.easyquant.model.MarketData;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 双均线策略实现类
 * 通过比较短期和长期移动平均线的相对位置和差距来产生交易信号
 * 计算过程：
 * 1. 计算短期和长期移动平均线
 * 2. 比较两条均线的相对位置和差距
 * - 当短期均线显著高于长期均线（差距超过1%）时产生买入信号
 * - 当短期均线显著低于长期均线（差距超过1%）时产生卖出信号
 * 这种策略相比简单的均线交叉更保守，能够避免频繁交易
 */
@Component
public class DoubleMAStrategy extends AbstractTradingStrategy {
    // 短期均线周期
    private final int shortPeriod;
    // 长期均线周期
    private final int longPeriod;
    // 存储短期均线价格队列
    private final Queue<Double> shortPrices = new LinkedList<>();
    // 存储长期均线价格队列
    private final Queue<Double> longPrices = new LinkedList<>();
    // 短期均线价格总和
    private double shortSum = 0;
    // 长期均线价格总和
    private double longSum = 0;

    /**
     * 默认构造函数，使用标准的双均线参数设置
     * shortPeriod=5: 5日短期均线
     * longPeriod=20: 20日长期均线
     */
    public DoubleMAStrategy() {
        this(5, 20);
    }

    /**
     * 自定义参数构造函数
     * 
     * @param shortPeriod 短期均线周期
     * @param longPeriod  长期均线周期
     */
    public DoubleMAStrategy(int shortPeriod, int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
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

        // 更新短期均线数据
        // 将新价格加入队列并更新总和
        shortPrices.offer(price);
        shortSum += price;
        // 当队列长度超过周期时，移除最早的数据
        if (shortPrices.size() > shortPeriod) {
            shortSum -= shortPrices.poll();
        }

        // 更新长期均线数据
        // 将新价格加入队列并更新总和
        longPrices.offer(price);
        longSum += price;
        // 当队列长度超过周期时，移除最早的数据
        if (longPrices.size() > longPeriod) {
            longSum -= longPrices.poll();
        }

        // 等待数据量达到要求的周期数
        if (shortPrices.size() < shortPeriod || longPrices.size() < longPeriod) {
            return null;
        }

        // 计算当前的短期和长期均线值
        double shortMA = shortSum / shortPeriod;
        double longMA = longSum / longPeriod;

        // 生成交易信号
        // 短期均线显著高于长期均线（差距超过1%），产生买入信号
        if (shortMA > longMA * 1.01) {
            return "BUY";
        }
        // 短期均线显著低于长期均线（差距超过1%），产生卖出信号
        else if (shortMA < longMA * 0.99) {
            return "SELL";
        }

        return null;
    }
}