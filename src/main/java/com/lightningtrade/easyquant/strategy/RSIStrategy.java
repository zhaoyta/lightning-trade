package com.lightningtrade.easyquant.strategy;

import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import com.lightningtrade.easyquant.model.MarketData;
import java.time.ZonedDateTime;
import java.time.ZoneId;

/**
 * RSI策略
 * 使用相对强弱指标（Relative Strength Index）产生交易信号
 * 
 * 策略逻辑：
 * 1. 当RSI值低于超卖线（默认30）时，产生买入信号
 * 2. 当RSI值高于超买线（默认70）时，产生卖出信号
 * 3. 当RSI值在超买超卖线之间时，不产生交易信号
 * 
 * 参数说明：
 * - RSI周期：14日（默认值）
 * - 超买线：70（默认值）
 * - 超卖线：30（默认值）
 */
@Component
public class RSIStrategy extends AbstractTradingStrategy {
    private final int period; // RSI计算周期
    private final double overbought; // 超买线
    private final double oversold; // 超卖线
    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private RSIIndicator rsi;

    /**
     * 默认构造函数
     * 使用14日RSI，30超卖，70超买作为默认参数
     */
    public RSIStrategy() {
        this(14, 30, 70);
    }

    /**
     * 自定义参数的构造函数
     * 
     * @param period     RSI计算周期
     * @param oversold   超卖线
     * @param overbought 超买线
     */
    public RSIStrategy(int period, double oversold, double overbought) {
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be positive");
        }
        if (oversold >= overbought) {
            throw new IllegalArgumentException("Overbought level must be greater than oversold level");
        }
        if (oversold < 0 || overbought > 100) {
            throw new IllegalArgumentException("RSI levels must be between 0 and 100");
        }

        this.period = period;
        this.oversold = oversold;
        this.overbought = overbought;
        this.series = new BaseBarSeriesBuilder().withName("RSI_Strategy").build();
        this.closePrice = new ClosePriceIndicator(series);
        this.rsi = new RSIIndicator(closePrice, period);
    }

    /**
     * 计算交易信号
     * 根据RSI值与超买超卖线的关系，生成买入或卖出信号
     * 
     * @param data 市场数据，包含最新的OHLCV数据
     * @return 交易信号：
     *         "BUY" - 买入信号，当RSI值低于超卖线
     *         "SELL" - 卖出信号，当RSI值高于超买线
     *         null - 无交易信号，RSI值在超买超卖线之间
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

        // 获取当前和前一个时间点的RSI值
        double currentRSI = rsi.getValue(index).doubleValue();
        double prevRSI = rsi.getValue(index - 1).doubleValue();

        // 判断RSI突破
        boolean crossDownOversold = prevRSI >= oversold && currentRSI < oversold; // RSI下穿超卖线
        boolean crossUpOverbought = prevRSI <= overbought && currentRSI > overbought; // RSI上穿超买线

        // 生成交易信号
        if (crossDownOversold) {
            return "BUY";
        } else if (crossUpOverbought) {
            return "SELL";
        }

        // 定期清理历史数据
        cleanHistoricalData();

        return null; // RSI值在超买超卖线之间，不产生信号
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