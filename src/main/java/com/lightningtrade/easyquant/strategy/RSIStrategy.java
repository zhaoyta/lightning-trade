package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
public class RSIStrategy extends AbstractTradingStrategy {
    private final int period;
    private final double overbought;
    private final double oversold;
    private final Queue<Double> gains = new LinkedList<>();
    private final Queue<Double> losses = new LinkedList<>();
    private Double lastPrice = null;
    private double sumGains = 0;
    private double sumLosses = 0;

    public RSIStrategy() {
        this(14, 70, 30); // 默认使用14日RSI，超买70，超卖30
    }

    public RSIStrategy(int period, double overbought, double oversold) {
        this.period = period;
        this.overbought = overbought;
        this.oversold = oversold;
    }

    @Override
    protected String calculateSignal(Map<String, Object> data) {
        double price = (double) data.get("close");

        if (lastPrice != null) {
            double change = price - lastPrice;
            double gain = Math.max(change, 0);
            double loss = Math.max(-change, 0);

            // 更新gains和losses队列
            gains.offer(gain);
            losses.offer(loss);
            sumGains += gain;
            sumLosses += loss;

            if (gains.size() > period) {
                sumGains -= gains.poll();
                sumLosses -= losses.poll();
            }

            if (gains.size() == period) {
                double avgGain = sumGains / period;
                double avgLoss = sumLosses / period;

                // 计算RSI
                double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
                double rsi = 100 - (100 / (1 + rs));

                // 生成交易信号
                if (rsi < oversold) {
                    return "BUY";
                } else if (rsi > overbought) {
                    return "SELL";
                }
            }
        }

        lastPrice = price;
        return null;
    }
}