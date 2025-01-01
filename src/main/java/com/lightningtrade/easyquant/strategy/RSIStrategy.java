package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;

/**
 * RSI策略
 * RSI < 30 超卖，买入信号
 * RSI > 70 超买，卖出信号
 */
public class RSIStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RSIStrategy.class);

    private final int period;
    private BarSeries series;
    private static final int OVERSOLD = 30; // 超卖线
    private static final int OVERBOUGHT = 70; // 超买线

    public RSIStrategy(int period) {
        this.period = period;
        logger.info("初始化RSI策略 - 周期: {}", period);
    }

    @Override
    public void initialize() {
        this.series = new BaseBarSeries();
        logger.info("初始化价格序列");
    }

    @Override
    public Signal execute(MarketData marketData) {
        // 更新价格序列
        series.addBar(marketData.getDateTime().atZone(ZoneId.systemDefault()),
                marketData.getOpen(),
                marketData.getHigh(),
                marketData.getLow(),
                marketData.getClose(),
                marketData.getVolume());

        logger.debug("添加新的价格数据 - 时间: {}, 收盘价: {}",
                marketData.getDateTime(),
                marketData.getClose());

        // 数据点不足时返回持仓信号
        if (series.getBarCount() < period) {
            logger.debug("数据点数量({})不足计算周期({}), 返回持仓信号",
                    series.getBarCount(), period);
            return Signal.HOLD;
        }

        // 计算RSI指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, period);

        int lastIndex = series.getEndIndex();
        double currentRsi = rsi.getValue(lastIndex).doubleValue();
        double prevRsi = rsi.getValue(lastIndex - 1).doubleValue();

        logger.debug("RSI计算结果: {}", currentRsi);

        // RSI从超卖区上穿，买入信号
        if (prevRsi <= OVERSOLD && currentRsi > OVERSOLD) {
            logger.info("产生买入信号 - RSI从超卖区上穿");
            logger.info("RSI: {} -> {}", prevRsi, currentRsi);
            return Signal.BUY;
        }
        // RSI从超买区下穿，卖出信号
        else if (prevRsi >= OVERBOUGHT && currentRsi < OVERBOUGHT) {
            logger.info("产生卖出信号 - RSI从超买区下穿");
            logger.info("RSI: {} -> {}", prevRsi, currentRsi);
            return Signal.SELL;
        }

        logger.debug("无交易信号");
        return Signal.HOLD;
    }
}