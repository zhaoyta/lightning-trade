package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
public class MACDStrategy extends AbstractTradingStrategy {
    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;
    private double fastEMA = 0;
    private double slowEMA = 0;
    private double signalEMA = 0;
    private Double lastMACD = null;
    private Double lastSignal = null;
    private boolean initialized = false;
    private int count = 0;

    public MACDStrategy() {
        this(12, 26, 9); // 默认使用12、26、9
    }

    public MACDStrategy(int fastPeriod, int slowPeriod, int signalPeriod) {
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
    }

    @Override
    protected String calculateSignal(Map<String, Object> data) {
        double price = (double) data.get("close");
        count++;

        if (!initialized) {
            if (count < slowPeriod) {
                // 累积足够的数据来初始化EMA
                return null;
            } else if (count == slowPeriod) {
                // 初始化EMA
                fastEMA = price;
                slowEMA = price;
                initialized = true;
                return null;
            }
        }

        // 更新EMA
        double fastAlpha = 2.0 / (fastPeriod + 1);
        double slowAlpha = 2.0 / (slowPeriod + 1);
        double signalAlpha = 2.0 / (signalPeriod + 1);

        fastEMA = price * fastAlpha + fastEMA * (1 - fastAlpha);
        slowEMA = price * slowAlpha + slowEMA * (1 - slowAlpha);

        // 计算MACD
        double macd = fastEMA - slowEMA;

        // 更新信号线
        if (lastMACD == null) {
            signalEMA = macd;
        } else {
            signalEMA = macd * signalAlpha + signalEMA * (1 - signalAlpha);
        }

        String signal = null;
        if (lastMACD != null && lastSignal != null) {
            // MACD金叉：MACD线从下穿过信号线
            if (lastMACD <= lastSignal && macd > signalEMA) {
                signal = "BUY";
            }
            // MACD死叉：MACD线从上穿过信号线
            else if (lastMACD >= lastSignal && macd < signalEMA) {
                signal = "SELL";
            }
        }

        lastMACD = macd;
        lastSignal = signalEMA;

        return signal;
    }
}