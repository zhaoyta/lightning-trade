package com.lightningtrade.easyquant.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 市场数据
 * 用于存储和传输单个时间点的市场行情数据
 */
@Data
public class MarketData {
    // 股票代码
    private String symbol;

    // 时间，格式：yyyy-MM-dd HH:mm:ss
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8", shape = JsonFormat.Shape.STRING)
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