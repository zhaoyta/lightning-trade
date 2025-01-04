package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.config.TradingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易策略工厂
 * 负责创建和管理不同类型的交易策略
 */
@Component
public class StrategyFactory {
    private static final Logger logger = LoggerFactory.getLogger(StrategyFactory.class);

    /**
     * 根据配置创建策略
     * 
     * @param config 策略配置
     * @return 交易策略实例
     */
    public TradingStrategy createStrategy(TradingConfig.Strategy config) {
        if (config == null || config.getType() == null) {
            throw new IllegalArgumentException("策略配置不能为空");
        }

        logger.info("创建策略实例 - 类型: {}, 参数: {}", config.getType(), config);

        switch (config.getType().toUpperCase()) {
            case "MA":
                return new MAStrategy(config.getShortPeriod());
            case "MA_CROSS":
                return new MACrossStrategy(config.getShortPeriod(), config.getLongPeriod());
            case "MACD":
                return new MACDStrategy(config.getShortPeriod(), config.getLongPeriod(), config.getSignalPeriod());
            case "RSI":
                return new RSIStrategy(config.getShortPeriod(), config.getOversoldThreshold(),
                        config.getOverboughtThreshold());
            case "DOUBLE_MA":
                return new DoubleMAStrategy(config.getShortPeriod(), config.getLongPeriod());
            case "BOLL":
                return new BollingerBandsStrategy(config.getShortPeriod(), config.getKValue());
            default:
                throw new IllegalArgumentException("不支持的策略类型: " + config.getType());
        }
    }

    /**
     * 获取所有支持的策略类型
     */
    public String[] getSupportedStrategyTypes() {
        return new String[] { "MA", "MA_CROSS", "MACD", "RSI", "DOUBLE_MA", "BOLL" };
    }

    /**
     * 获取策略的默认参数
     */
    public Map<String, Object> getDefaultParameters(String strategyType) {
        Map<String, Object> params = new HashMap<>();
        switch (strategyType.toUpperCase()) {
            case "MA":
                params.put("period", 20);
                break;
            case "MA_CROSS":
                params.put("shortPeriod", 5);
                params.put("longPeriod", 20);
                break;
            case "MACD":
                params.put("shortPeriod", 12);
                params.put("longPeriod", 26);
                params.put("signalPeriod", 9);
                break;
            case "RSI":
                params.put("period", 14);
                params.put("oversoldThreshold", 30);
                params.put("overboughtThreshold", 70);
                break;
            case "DOUBLE_MA":
                params.put("shortPeriod", 5);
                params.put("longPeriod", 20);
                break;
            case "BOLL":
                params.put("period", 20);
                params.put("kValue", 2.0);
                break;
            default:
                throw new IllegalArgumentException("不支持的策略类型: " + strategyType);
        }
        return params;
    }
}