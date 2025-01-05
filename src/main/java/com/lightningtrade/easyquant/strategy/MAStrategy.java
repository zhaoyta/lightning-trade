package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import com.lightningtrade.easyquant.model.MarketData;
import java.time.ZonedDateTime;
import java.time.ZoneId;

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
    private final int period;
    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private SMAIndicator sma;

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
        this.series = new BaseBarSeriesBuilder().withName("MA_Strategy").build();
        this.closePrice = new ClosePriceIndicator(series);
        this.sma = new SMAIndicator(closePrice, period);
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

        // 获取最新价格和均线值
        double price = data.getClose();
        double ma = sma.getValue(index).doubleValue();

        // 生成交易信号
        if (price > ma * 1.02) { // 价格高于MA 2%时买入
            return "BUY";
        } else if (price < ma * 0.98) { // 价格低于MA 2%时卖出
            return "SELL";
        }

        // 定期清理历史数据
        cleanHistoricalData();

        return null; // 价格在MA的2%范围内，不产生信号
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