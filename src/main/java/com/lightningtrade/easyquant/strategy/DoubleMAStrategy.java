package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;

/**
 * 双均线策略
 * 使用EMA而不是SMA，对价格变化更敏感
 * 当快线上穿慢线时买入
 * 当快线下穿慢线时卖出
 */
public class DoubleMAStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DoubleMAStrategy.class);

    private final int shortPeriod;
    private final int longPeriod;
    private BarSeries series;

    public DoubleMAStrategy(int shortPeriod, int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        logger.info("初始化双均线策略 - 快线: {}, 慢线: {}", shortPeriod, longPeriod);
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
            logger.debug("数据点数量({})不足长期均线周期({}), 返回持仓信号",
                    series.getBarCount(), longPeriod);
            return Signal.HOLD;
        }

        // 计算双均线
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator shortEma = new EMAIndicator(closePrice, shortPeriod);
        EMAIndicator longEma = new EMAIndicator(closePrice, longPeriod);

        int lastIndex = series.getEndIndex();
        double shortEmaValue = shortEma.getValue(lastIndex).doubleValue();
        double longEmaValue = longEma.getValue(lastIndex).doubleValue();
        double prevShortEmaValue = shortEma.getValue(lastIndex - 1).doubleValue();
        double prevLongEmaValue = longEma.getValue(lastIndex - 1).doubleValue();

        logger.debug("均线计算结果 - 快线: {}, 慢线: {}", shortEmaValue, longEmaValue);

        // 快线上穿慢线，买入信号
        if (prevShortEmaValue <= prevLongEmaValue && shortEmaValue > longEmaValue) {
            logger.info("产生买入信号 - 快线上穿慢线");
            logger.info("快线: {} -> {}, 慢线: {} -> {}",
                    prevShortEmaValue, shortEmaValue,
                    prevLongEmaValue, longEmaValue);
            return Signal.BUY;
        }
        // 快线下穿慢线，卖出信号
        else if (prevShortEmaValue >= prevLongEmaValue && shortEmaValue < longEmaValue) {
            logger.info("产生卖出信号 - 快线下穿慢线");
            logger.info("快线: {} -> {}, 慢线: {} -> {}",
                    prevShortEmaValue, shortEmaValue,
                    prevLongEmaValue, longEmaValue);
            return Signal.SELL;
        }

        logger.debug("无交易信号");
        return Signal.HOLD;
    }
}