package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;

@Component
public class MACrossStrategy extends AbstractTradingStrategy {
    private final int shortPeriod;
    private final int longPeriod;
    private final Queue<Double> shortPrices = new LinkedList<>();
    private final Queue<Double> longPrices = new LinkedList<>();
    private double shortSum = 0;
    private double longSum = 0;
    private Double lastShortMA = null;
    private Double lastLongMA = null;

    public MACrossStrategy() {
        this(5, 20); // 默认使用5日和20日均线
    }

    public MACrossStrategy(int shortPeriod, int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
    }

    @Override
    protected String calculateSignal(Map<String, Object> data) {
        double price = (double) data.get("close");

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

        // 等待足够的数据
        if (shortPrices.size() < shortPeriod || longPrices.size() < longPeriod) {
            return null;
        }

        // 计算均线
        double shortMA = shortSum / shortPeriod;
        double longMA = longSum / longPeriod;

        String signal = null;
        if (lastShortMA != null && lastLongMA != null) {
            // 金叉：短期均线从下穿过长期均线
            if (lastShortMA <= lastLongMA && shortMA > longMA) {
                signal = "BUY";
            }
            // 死叉：短期均线从上穿过长期均线
            else if (lastShortMA >= lastLongMA && shortMA < longMA) {
                signal = "SELL";
            }
        }

        // 更新上一次的均线值
        lastShortMA = shortMA;
        lastLongMA = longMA;

        return signal;
    }
}