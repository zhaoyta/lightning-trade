package com.lightningtrade.easyquant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "trading")
public class TradingConfig {
    private Map<String, Market> markets;

    @Data
    public static class Market {
        private String name;
        private boolean enabled;
        private Map<String, Symbol> symbols;
        private Strategy strategy;
    }

    @Data
    public static class Symbol {
        private String code;
        private int lotSize;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Symbol symbol = (Symbol) o;
            return code != null && code.equals(symbol.code);
        }

        @Override
        public int hashCode() {
            return code != null ? code.hashCode() : 0;
        }
    }

    @Data
    public static class Strategy {
        private String type;
        private Integer shortPeriod;
        private Integer longPeriod;
        private Integer signalPeriod;
        private Double oversoldThreshold;
        private Double overboughtThreshold;
        private Double kValue;
        private String market;
        private String kType = "day"; // 默认使用日K线

        public String getKType() {
            return kType;
        }

        public void setKType(String kType) {
            this.kType = kType;
        }
    }
}