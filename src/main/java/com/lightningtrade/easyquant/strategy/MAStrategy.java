package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import com.lightningtrade.easyquant.model.MarketData;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 移动平均线策略
 * 使用单一移动平均线作为基准，当价格与均线产生一定幅度的偏离时产生交易信号
 * 
 * 策略逻辑：
 * 1. 当价格上涨超过均线2%时，产生买入信号（上涨趋势确认）
 * 2. 当价格下跌超过均线2%时，产生卖出信号（下跌趋势确认）
 * 3. 价格在均线2%范围内波动时，不产生交易信号（震荡区间）
 */
@Component
public class MAStrategy extends AbstractTradingStrategy {
    /**
     * 移动平均线周期
     * 默认使用20日均线，可通过构造函数自定义
     */
    private final int period;

    /**
     * 用于存储历史价格的队列
     * 当新价格进入时，最老的价格将被移除，保持队列长度等于周期
     */
    private final Queue<Double> prices = new LinkedList<>();

    /**
     * 价格总和
     * 用于快速计算移动平均线，避免每次都遍历队列求和
     */
    private double sum = 0;

    /**
     * 默认构造函数
     * 使用20日均线作为默认参数
     */
    public MAStrategy() {
        this(20); // 默认使用20日均线
    }

    /**
     * 自定义周期的构造函数
     * 
     * @param period 移动平均线的周期，如20表示20日均线
     */
    public MAStrategy(int period) {
        this.period = period;
    }

    /**
     * 计算交易信号
     * 根据最新价格和移动平均线的关系，生成买入或卖出信号
     * 
     * @param data 市场数据，包含最新的OHLCV数据
     * @return 交易信号：
     *         "BUY" - 买入信号，当价格上涨超过均线2%
     *         "SELL" - 卖出信号，当价格下跌超过均线2%
     *         null - 无交易信号，价格在均线2%范围内波动
     */
    @Override
    protected String calculateSignal(MarketData data) {
        // 获取最新收盘价
        double price = data.getClose();

        // 更新移动平均线数据
        prices.offer(price);
        sum += price;

        // 当队列长度超过周期时，移除最老的价格
        if (prices.size() > period) {
            sum -= prices.poll();
        }

        // 在累积足够的数据之前，不产生交易信号
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

        return null; // 价格在MA的2%范围内，不产生信号
    }
}