package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;

/**
 * 布林带策略
 * 价格突破上轨，卖出信号（超买）
 * 价格突破下轨，买入信号（超卖）
 */
public class BollingerBandsStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(BollingerBandsStrategy.class);

    private final int period; // 移动平均期数
    private final double k; // 标准差倍数
    private BarSeries series;

    public BollingerBandsStrategy(int period, double k) {
        this.period = period;
        this.k = k;
        logger.info("初始化布林带策略 - 周期: {}, 标准差倍数: {}", period, k);
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

        // 计算布林带指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, period);
        BollingerBandsMiddleIndicator middleBand = new BollingerBandsMiddleIndicator(sma);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, period);
        BollingerBandsUpperIndicator upperBand = new BollingerBandsUpperIndicator(middleBand, sd);
        BollingerBandsLowerIndicator lowerBand = new BollingerBandsLowerIndicator(middleBand, sd);

        int lastIndex = series.getEndIndex();
        double currentPrice = closePrice.getValue(lastIndex).doubleValue();
        double prevPrice = closePrice.getValue(lastIndex - 1).doubleValue();
        double upperValue = upperBand.getValue(lastIndex).doubleValue();
        double lowerValue = lowerBand.getValue(lastIndex).doubleValue();
        double prevUpperValue = upperBand.getValue(lastIndex - 1).doubleValue();
        double prevLowerValue = lowerBand.getValue(lastIndex - 1).doubleValue();

        logger.debug("布林带计算结果 - 上轨: {}, 当前价: {}, 下轨: {}",
                upperValue, currentPrice, lowerValue);

        // 价格从下轨下方向上突破，买入信号
        if (prevPrice <= prevLowerValue && currentPrice > lowerValue) {
            logger.info("产生买入信号 - 价格突破下轨");
            logger.info("价格: {} -> {}, 下轨: {} -> {}",
                    prevPrice, currentPrice, prevLowerValue, lowerValue);
            return Signal.BUY;
        }
        // 价格从上轨上方向下突破，卖出信号
        else if (prevPrice >= prevUpperValue && currentPrice < upperValue) {
            logger.info("产生卖出信号 - 价格突破上轨");
            logger.info("价格: {} -> {}, 上轨: {} -> {}",
                    prevPrice, currentPrice, prevUpperValue, upperValue);
            return Signal.SELL;
        }

        logger.debug("无交易信号");
        return Signal.HOLD;
    }
}