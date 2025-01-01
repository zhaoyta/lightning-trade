package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import java.time.ZoneId;

/**
 * 均线交叉策略
 */
public class MACrossStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MACrossStrategy.class);

    private final int shortPeriod;
    private final int longPeriod;
    private BarSeries series;

    public MACrossStrategy(int shortPeriod, int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        logger.info("初始化均线交叉策略 - 短期均线周期: {}, 长期均线周期: {}", shortPeriod, longPeriod);
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

        // 数据点不足时返回空信号
        if (series.getBarCount() < longPeriod) {
            logger.debug("数据点数量({})不足长期均线周期({}), 返回空信号",
                    series.getBarCount(), longPeriod);
            return Signal.NONE;
        }

        // 计算短期和长期均线
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, shortPeriod);
        SMAIndicator longSma = new SMAIndicator(closePrice, longPeriod);

        int lastIndex = series.getEndIndex();
        double shortSmaValue = shortSma.getValue(lastIndex).doubleValue();
        double longSmaValue = longSma.getValue(lastIndex).doubleValue();
        double prevShortSmaValue = shortSma.getValue(lastIndex - 1).doubleValue();
        double prevLongSmaValue = longSma.getValue(lastIndex - 1).doubleValue();

        logger.debug("均线计算结果 - 短期均线: {}, 长期均线: {}", shortSmaValue, longSmaValue);

        // 判断均线交叉生成信号
        if (shortSma.getValue(lastIndex).isGreaterThan(longSma.getValue(lastIndex)) &&
                shortSma.getValue(lastIndex - 1).isLessThan(longSma.getValue(lastIndex - 1))) {
            logger.info("产生买入信号 - 短期均线上穿长期均线");
            logger.info("短期均线: {} -> {}, 长期均线: {} -> {}",
                    prevShortSmaValue, shortSmaValue,
                    prevLongSmaValue, longSmaValue);
            return Signal.BUY;
        } else if (shortSma.getValue(lastIndex).isLessThan(longSma.getValue(lastIndex)) &&
                shortSma.getValue(lastIndex - 1).isGreaterThan(longSma.getValue(lastIndex - 1))) {
            logger.info("产生卖出信号 - 短期均线下穿长期均线");
            logger.info("短期均线: {} -> {}, 长期均线: {} -> {}",
                    prevShortSmaValue, shortSmaValue,
                    prevLongSmaValue, longSmaValue);
            return Signal.SELL;
        }

        logger.debug("无交易信号");
        return Signal.HOLD;
    }
}