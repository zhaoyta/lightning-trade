package com.lightningtrade.easyquant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "trading")
public class TradingConfig {
    private Market us;
    private Market hk;

    @Data
    public static class Market {
        private boolean enabled;
        private List<Symbol> symbols;
        private Strategy strategy;
    }

    @Data
    public static class Symbol {
        private String symbol;
        private double positionRatio;
        private int maxPosition;
        private int lotSize;
    }

    @Data
    public static class Strategy {
        private String type = "MA"; // 策略类型：MA, MACD, RSI, DOUBLE_MA, BOLL
        private int shortPeriod = 5; // 短期周期
        private int longPeriod = 20; // 长期周期
        private double kValue = 2.0; // 布林带标准差倍数
    }
}