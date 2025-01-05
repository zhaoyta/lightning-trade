package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import com.lightningtrade.easyquant.model.MarketData;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * MACD策略
 * 使用MACD指标（Moving Average Convergence/Divergence）产生交易信号
 * 
 * 策略逻辑：
 * 1. 当MACD线上穿信号线时，产生买入信号（看涨信号）
 * 2. 当MACD线下穿信号线时，产生卖出信号（看跌信号）
 * 3. 在MACD线和信号线未发生交叉时，不产生交易信号
 * 
 * MACD参数说明：
 * - 快速EMA周期：12（用于计算DIF）
 * - 慢速EMA周期：26（用于计算DIF）
 * - 信号线EMA周期：9（用于计算DEA）
 * - DIF = 快速EMA - 慢速EMA
 * - DEA = DIF的9日EMA
 * - MACD = 2 * (DIF - DEA)
 */
@Component
public class MACDStrategy extends AbstractTradingStrategy {
    private final int fastPeriod; // 快速EMA周期
    private final int slowPeriod; // 慢速EMA周期
    private final int signalPeriod; // 信号线周期
    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private MACDIndicator macd;
    private EMAIndicator signal;

    /**
     * 默认构造函数
     * 使用标准的MACD参数：12,26,9
     */
    public MACDStrategy() {
        this(12, 26, 9);
    }

    /**
     * 自定义参数的构造函数
     * 
     * @param fastPeriod   快速EMA周期
     * @param slowPeriod   慢速EMA周期
     * @param signalPeriod 信号线周期
     */
    public MACDStrategy(int fastPeriod, int slowPeriod, int signalPeriod) {
        if (fastPeriod >= slowPeriod) {
            throw new IllegalArgumentException("Fast period must be less than slow period");
        }
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
        this.series = new BaseBarSeriesBuilder().withName("MACD_Strategy").build();
        this.closePrice = new ClosePriceIndicator(series);
        this.macd = new MACDIndicator(closePrice, fastPeriod, slowPeriod);
        this.signal = new EMAIndicator(macd, signalPeriod);
    }

    /**
     * 计算交易信号
     * 根据MACD线和信号线的交叉关系，生成买入或卖出信号
     * 
     * @param data 市场数据，包含最新的OHLCV数据
     * @return 交易信号：
     *         "BUY" - 买入信号，当MACD线上穿信号线
     *         "SELL" - 卖出信号，当MACD线下穿信号线
     *         null - 无交易信号，MACD线和信号线未发生交叉
     */
    @Override
    protected String calculateSignal(MarketData data) {
        // 添加新的K线数据
        ZonedDateTime dateTime = data.getDateTime().atZone(ZoneId.systemDefault());
        series.addBar(dateTime,
                data.getOpen(),
                data.getHigh(),
                data.getLow(),
                data.getClose(),
                data.getVolume());

        // 在累积足够的数据之前，不产生交易信号
        int index = series.getEndIndex();
        if (index < slowPeriod + signalPeriod) {
            return null;
        }

        // 获取当前和前一个时间点的MACD值和信号线值
        double macdValue = macd.getValue(index).doubleValue();
        double signalValue = signal.getValue(index).doubleValue();
        double prevMacdValue = macd.getValue(index - 1).doubleValue();
        double prevSignalValue = signal.getValue(index - 1).doubleValue();

        // 判断MACD线和信号线的交叉
        boolean crossUp = prevMacdValue <= prevSignalValue && macdValue > signalValue; // MACD线上穿信号线
        boolean crossDown = prevMacdValue >= prevSignalValue && macdValue < signalValue; // MACD线下穿信号线

        // 生成交易信号
        if (crossUp) {
            return "BUY";
        } else if (crossDown) {
            return "SELL";
        }

        // 定期清理历史数据
        cleanHistoricalData();

        return null; // MACD线和信号线未发生交叉，不产生信号
    }

    /**
     * 清理历史数据
     * 当数据量过大时，清理旧数据以节省内存
     */
    private void cleanHistoricalData() {
        int maxPeriod = Math.max(slowPeriod + signalPeriod, fastPeriod + signalPeriod);
        if (series.getBarCount() > maxPeriod * 2) {
            // 保留最近的两倍周期数据
            int removeCount = series.getBarCount() - maxPeriod * 2;
            for (int i = 0; i < removeCount; i++) {
                series.getBar(0); // 移除最早的数据
            }
        }
    }
}