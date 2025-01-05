package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import com.lightningtrade.easyquant.model.MarketData;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * 双均线交叉策略
 * 使用短期和长期两条移动平均线，根据它们的交叉产生交易信号
 * 
 * 策略逻辑：
 * 1. 当短期均线上穿长期均线时，产生买入信号（上涨趋势开始）
 * 2. 当短期均线下穿长期均线时，产生卖出信号（下跌趋势开始）
 * 3. 在均线未发生交叉时，不产生交易信号（趋势延续）
 */
@Component
public class MACrossStrategy extends AbstractTradingStrategy {
    private final int shortPeriod;
    private final int longPeriod;
    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private SMAIndicator shortSMA;
    private SMAIndicator longSMA;

    /**
     * 默认构造函数
     * 使用5日和20日均线作为默认参数
     */
    public MACrossStrategy() {
        this(5, 20); // 默认使用5日和20日均线
    }

    /**
     * 自定义周期的构造函数
     * 
     * @param shortPeriod 短期均线周期
     * @param longPeriod  长期均线周期
     */
    public MACrossStrategy(int shortPeriod, int longPeriod) {
        if (shortPeriod >= longPeriod) {
            throw new IllegalArgumentException("Short period must be less than long period");
        }
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.series = new BaseBarSeriesBuilder().withName("MACross_Strategy").build();
        this.closePrice = new ClosePriceIndicator(series);
        this.shortSMA = new SMAIndicator(closePrice, shortPeriod);
        this.longSMA = new SMAIndicator(closePrice, longPeriod);
    }

    /**
     * 计算交易信号
     * 根据短期和长期移动平均线的交叉关系，生成买入或卖出信号
     * 
     * @param data 市场数据，包含最新的OHLCV数据
     * @return 交易信号：
     *         "BUY" - 买入信号，当短期均线上穿长期均线
     *         "SELL" - 卖出信号，当短期均线下穿长期均线
     *         null - 无交易信号，均线未发生交叉
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
        if (index < longPeriod) {
            return null;
        }

        // 获取当前和前一个时间点的均线值
        double shortMA = shortSMA.getValue(index).doubleValue();
        double longMA = longSMA.getValue(index).doubleValue();
        double prevShortMA = shortSMA.getValue(index - 1).doubleValue();
        double prevLongMA = longSMA.getValue(index - 1).doubleValue();

        // 判断均线交叉
        boolean crossUp = prevShortMA <= prevLongMA && shortMA > longMA; // 短期均线上穿长期均线
        boolean crossDown = prevShortMA >= prevLongMA && shortMA < longMA; // 短期均线下穿长期均线

        // 生成交易信号
        if (crossUp) {
            return "BUY";
        } else if (crossDown) {
            return "SELL";
        }

        // 定期清理历史数据
        cleanHistoricalData();

        return null; // 均线未发生交叉，不产生信号
    }

    /**
     * 清理历史数据
     * 当数据量过大时，清理旧数据以节省内存
     */
    private void cleanHistoricalData() {
        if (series.getBarCount() > longPeriod * 2) {
            // 保留最近的两倍周期数据
            int removeCount = series.getBarCount() - longPeriod * 2;
            for (int i = 0; i < removeCount; i++) {
                series.getBar(0); // 移除最早的数据
            }
        }
    }
}