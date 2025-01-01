package com.lightningtrade.easyquant.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 市场数据
 */
@Data
public class MarketData {
    // 股票代码
    private String symbol;

    // 时间
    private LocalDateTime dateTime;

    // 开盘价
    private double open;

    // 最高价
    private double high;

    // 最低价
    private double low;

    // 收盘价
    private double close;

    // 成交量
    private long volume;

    // 成交额
    private double amount;
}