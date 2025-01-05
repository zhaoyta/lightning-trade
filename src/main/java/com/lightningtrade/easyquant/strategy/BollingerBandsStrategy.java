package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import com.lightningtrade.easyquant.model.MarketData;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * 布林带策略
 * 使用布林带指标（Bollinger Bands）产生交易信号
 * 
 * 策略逻辑：
 * 1. 当价格突破布林带下轨时，产生买入信号（超卖）
 * 2. 当价格突破布林带上轨时，产生卖出信号（超买）
 * 3. 当价格在布林带中轨附近时，不产生交易信号
 * 
 * 参数说明：
 * - 移动平均线周期：20日（默认值）
 * - 标准差倍数：2（默认值）
 * - 上轨 = 中轨 + 2 * 标准差
 * - 下轨 = 中轨 - 2 * 标准差
 * - 中轨 = 20日简单移动平均线
 */
@Component
public class BollingerBandsStrategy extends AbstractTradingStrategy {
    private final int period; // 移动平均线周期
    private final double multiplier; // 标准差倍数
    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private BollingerBandsMiddleIndicator middleBand;
    private BollingerBandsUpperIndicator upperBand;
    private BollingerBandsLowerIndicator lowerBand;

    /**
     * 默认构造函数
     * 使用20日均线和2倍标准差作为默认参数
     */
    public BollingerBandsStrategy() {
        this(20, 2.0);
    }

    /**
     * 自定义参数的构造函数
     * 
     * @param period     移动平均线周期
     * @param multiplier 标准差倍数
     */
    public BollingerBandsStrategy(int period, double multiplier) {
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be positive");
        }
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }
        this.period = period;
        this.multiplier = multiplier;
        this.series = new BaseBarSeriesBuilder().withName("BollingerBands_Strategy").build();
        this.closePrice = new ClosePriceIndicator(series);

        // 计算布林带指标
        SMAIndicator sma = new SMAIndicator(closePrice, period);
        StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice, period);
        this.middleBand = new BollingerBandsMiddleIndicator(sma);
        this.upperBand = new BollingerBandsUpperIndicator(this.middleBand, standardDeviation);
        this.lowerBand = new BollingerBandsLowerIndicator(this.middleBand, standardDeviation);
    }

    /**
     * 计算交易信号
     * 根据价格与布林带上下轨的关系，生成买入或卖出信号
     * 
     * @param data 市场数据，包含最新的OHLCV数据
     * @return 交易信号：
     *         "BUY" - 买入信号，当价格突破布林带下轨
     *         "SELL" - 卖出信号，当价格突破布林带上轨
     *         null - 无交易信号，价格在布林带中轨附近
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
        if (index < period) {
            return null;
        }

        // 获取当前价格和布林带值
        double price = closePrice.getValue(index).doubleValue();
        double upper = upperBand.getValue(index).doubleValue();
        double lower = lowerBand.getValue(index).doubleValue();

        // 获取前一个时间点的价格和布林带值
        double prevPrice = closePrice.getValue(index - 1).doubleValue();
        double prevUpper = upperBand.getValue(index - 1).doubleValue();
        double prevLower = lowerBand.getValue(index - 1).doubleValue();

        // 判断价格突破
        boolean breakLower = prevPrice >= prevLower && price < lower; // 价格突破下轨
        boolean breakUpper = prevPrice <= prevUpper && price > upper; // 价格突破上轨

        // 生成交易信号
        if (breakLower) {
            return "BUY";
        } else if (breakUpper) {
            return "SELL";
        }

        // 定期清理历史数据
        cleanHistoricalData();

        return null; // 价格在布林带中轨附近，不产生信号
    }

    /**
     * 清理历史数据
     * 当数据量过大时，清理旧数据以节省内存
     */
    private void cleanHistoricalData() {
        if (series.getBarCount() > period * 2) {
            // 保留最近的两倍周期数据
            int removeCount = series.getBarCount() - period * 2;
            for (int i = 0; i < removeCount; i++) {
                series.getBar(0); // 移除最早的数据
            }
        }
    }
}