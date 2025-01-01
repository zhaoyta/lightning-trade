package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.config.TradingConfig;
import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {

    public TradingStrategy createStrategy(TradingConfig.Strategy config) {
        switch (config.getType().toUpperCase()) {
            case "MA":
                return new MACrossStrategy(config.getShortPeriod(), config.getLongPeriod());
            case "MACD":
                return new MACDStrategy(config.getShortPeriod(), config.getLongPeriod());
            case "RSI":
                return new RSIStrategy(config.getShortPeriod());
            case "DOUBLE_MA":
                return new DoubleMAStrategy(config.getShortPeriod(), config.getLongPeriod());
            case "BOLL":
                return new BollingerBandsStrategy(config.getShortPeriod(), config.getKValue());
            default:
                throw new IllegalArgumentException("不支持的策略类型: " + config.getType());
        }
    }
}