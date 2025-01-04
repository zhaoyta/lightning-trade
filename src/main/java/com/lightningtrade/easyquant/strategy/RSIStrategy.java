package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import com.lightningtrade.easyquant.model.MarketData;
import java.util.LinkedList;
import java.util.Queue;

/**
 * RSI (Relative Strength Index) 策略实现类
 * RSI是一种动量指标，用于衡量价格变动的强度，判断市场是否超买或超卖
 * 计算过程：
 * 1. 计算每日价格变动的涨跌幅
 * 2. 分别计算上涨和下跌的平均值
 * 3. RSI = 100 - (100 / (1 + RS))，其中RS = 平均上涨幅度 / 平均下跌幅度
 * - 当RSI低于超卖线时产生买入信号
 * - 当RSI高于超买线时产生卖出信号
 */
@Component
public class RSIStrategy extends AbstractTradingStrategy {
    // RSI计算周期
    private final int period;
    // RSI超买阈值
    private final double overbought;
    // RSI超卖阈值
    private final double oversold;
    // 存储周期内的上涨幅度队列
    private final Queue<Double> gains = new LinkedList<>();
    // 存储周期内的下跌幅度队列
    private final Queue<Double> losses = new LinkedList<>();
    // 上一次的价格
    private Double lastPrice = null;
    // 周期内上涨幅度之和
    private double sumGains = 0;
    // 周期内下跌幅度之和
    private double sumLosses = 0;

    /**
     * 默认构造函数，使用标准的RSI参数设置
     * period=14: 14日RSI
     * overbought=70: RSI高于70视为超买
     * oversold=30: RSI低于30视为超卖
     */
    public RSIStrategy() {
        this(14, 70, 30); // 默认使用14日RSI，超买70，超卖30
    }

    /**
     * 自定义参数构造函数
     * 
     * @param period     RSI计算周期
     * @param overbought 超买阈值
     * @param oversold   超卖阈值
     */
    public RSIStrategy(int period, double overbought, double oversold) {
        this.period = period;
        this.overbought = overbought;
        this.oversold = oversold;
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

        // 需要两个价格点才能计算价格变动
        if (lastPrice != null) {
            // 计算价格变动和涨跌幅
            double change = price - lastPrice;
            // 上涨幅度（价格上涨时为正值，下跌时为0）
            double gain = Math.max(change, 0);
            // 下跌幅度（价格下跌时为正值，上涨时为0）
            double loss = Math.max(-change, 0);

            // 更新gains和losses队列
            // 将新的涨跌幅加入队列并更新总和
            gains.offer(gain);
            losses.offer(loss);
            sumGains += gain;
            sumLosses += loss;

            // 当队列长度超过周期时，移除最早的数据
            if (gains.size() > period) {
                sumGains -= gains.poll();
                sumLosses -= losses.poll();
            }

            // 当收集到足够的数据点时开始计算RSI
            if (gains.size() == period) {
                // 计算平均涨跌幅
                double avgGain = sumGains / period;
                double avgLoss = sumLosses / period;

                // 计算RSI
                // 当没有下跌时，RSI = 100
                double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
                double rsi = 100 - (100 / (1 + rs));

                // 生成交易信号
                // RSI低于超卖线，市场超卖，产生买入信号
                if (rsi < oversold) {
                    return "BUY";
                }
                // RSI高于超买线，市场超买，产生卖出信号
                else if (rsi > overbought) {
                    return "SELL";
                }
            }
        }

        // 保存当前价格用于下次计算
        lastPrice = price;
        return null;
    }
}