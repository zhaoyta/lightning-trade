package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import com.lightningtrade.easyquant.model.MarketData;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 均线交叉策略实现类
 * 通过比较短期和长期移动平均线的交叉来产生交易信号
 * - 当短期均线从下向上穿过长期均线时产生买入信号（金叉）
 * - 当短期均线从上向下穿过长期均线时产生卖出信号（死叉）
 */
@Component
public class MACrossStrategy extends AbstractTradingStrategy {
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
    // 上一次短期均线值
    private Double lastShortMA = null;
    // 上一次长期均线值
    private Double lastLongMA = null;

    /**
     * 默认构造函数，使用5日和20日均线
     */
    public MACrossStrategy() {
        this(5, 20); // 默认使用5日和20日均线
    }

    /**
     * 自定义周期构造函数
     * 
     * @param shortPeriod 短期均线周期
     * @param longPeriod  长期均线周期
     */
    public MACrossStrategy(int shortPeriod, int longPeriod) {
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
        shortPrices.offer(price);
        shortSum += price;
        if (shortPrices.size() > shortPeriod) {
            shortSum -= shortPrices.poll();
        }

        // 更新长期均线数据
        longPrices.offer(price);
        longSum += price;
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

        String signal = null;
        if (lastShortMA != null && lastLongMA != null) {
            // 金叉：短期均线从下穿过长期均线，产生买入信号
            if (lastShortMA <= lastLongMA && shortMA > longMA) {
                signal = "BUY";
            }
            // 死叉：短期均线从上穿过长期均线，产生卖出信号
            else if (lastShortMA >= lastLongMA && shortMA < longMA) {
                signal = "SELL";
            }
        }

        // 保存当前均线值，用于下次计算
        lastShortMA = shortMA;
        lastLongMA = longMA;

        return signal;
    }
}