package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;

@Component
public class MAStrategy extends AbstractTradingStrategy {
    private final int period;
    private final Queue<Double> prices = new LinkedList<>();
    private double sum = 0;

    public MAStrategy() {
        this(20); // 默认使用20日均线
    }

    public MAStrategy(int period) {
        this.period = period;
    }

    @Override
    protected String calculateSignal(Map<String, Object> data) {
        double price = (double) data.get("close");

        // 更新移动平均线数据
        prices.offer(price);
        sum += price;

        if (prices.size() > period) {
            sum -= prices.poll();
        }

        if (prices.size() < period) {
            return null;
        }

        // 计算移动平均线
        double ma = sum / period;

        // 生成交易信号
        if (price > ma * 1.02) { // 价格高于MA 2%时买入
            return "BUY";
        } else if (price < ma * 0.98) { // 价格低于MA 2%时卖出
            return "SELL";
        }

        return null;
    }
}