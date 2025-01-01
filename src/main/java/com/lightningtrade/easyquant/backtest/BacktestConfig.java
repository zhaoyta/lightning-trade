package com.lightningtrade.easyquant.backtest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "backtest")
public class BacktestConfig {

    private boolean enabled;

    // 初始资金
    private double initialCapital;

    // 回测开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    // 回测结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // 回测市场 (US/HK)
    private String market;

    // 回测标的
    private Map<String, SymbolConfig> symbols;

    @Data
    public static class SymbolConfig {
        // 股票代码
        private String symbol;

        // 每手股数（仅港股需要）
        private int lotSize;

        // 初始持仓
        private int initialPosition;
    }
}