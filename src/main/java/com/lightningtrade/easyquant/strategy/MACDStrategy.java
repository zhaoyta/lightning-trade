package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;

/**
 * MACD策略
 * 当MACD线上穿信号线时买入
 * 当MACD线下穿信号线时卖出
 */
public class MACDStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MACDStrategy.class);

    private final int shortPeriod;
    private final int longPeriod;
    private BarSeries series;

    public MACDStrategy(int shortPeriod, int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        logger.info("初始化MACD策略 - 短期: {}, 长期: {}", shortPeriod, longPeriod);
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
        if (series.getBarCount() < longPeriod) {
            logger.debug("数据点数量({})不足长期周期({}), 返回持仓信号",
                    series.getBarCount(), longPeriod);
            return Signal.HOLD;
        }

        // 计算MACD指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, shortPeriod, longPeriod);
        EMAIndicator signal = new EMAIndicator(macd, 9); // 信号线，一般用9日EMA

        int lastIndex = series.getEndIndex();
        double macdValue = macd.getValue(lastIndex).doubleValue();
        double signalValue = signal.getValue(lastIndex).doubleValue();
        double prevMacdValue = macd.getValue(lastIndex - 1).doubleValue();
        double prevSignalValue = signal.getValue(lastIndex - 1).doubleValue();

        logger.debug("MACD计算结果 - MACD线: {}, 信号线: {}", macdValue, signalValue);

        // MACD线上穿信号线，买入信号
        if (prevMacdValue <= prevSignalValue && macdValue > signalValue) {
            logger.info("产生买入信号 - MACD线上穿信号线");
            logger.info("MACD: {} -> {}, 信号线: {} -> {}",
                    prevMacdValue, macdValue,
                    prevSignalValue, signalValue);
            return Signal.BUY;
        }
        // MACD线下穿信号线，卖出信号
        else if (prevMacdValue >= prevSignalValue && macdValue < signalValue) {
            logger.info("产生卖出信号 - MACD线下穿信号线");
            logger.info("MACD: {} -> {}, 信号线: {} -> {}",
                    prevMacdValue, macdValue,
                    prevSignalValue, signalValue);
            return Signal.SELL;
        }

        logger.debug("无交易信号");
        return Signal.HOLD;
    }
}