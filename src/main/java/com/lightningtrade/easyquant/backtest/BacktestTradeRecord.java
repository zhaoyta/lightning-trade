package com.lightningtrade.easyquant.backtest;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 回测交易记录类
 * 用于记录回测过程中的每笔交易信息，包括交易时间、类型、价格等
 * 
 * 主要用途：
 * 1. 记录回测过程中的交易执行情况
 * 2. 用于计算策略的绩效指标（如胜率、盈亏比等）
 * 3. 为交易分析和策略优化提供数据支持
 * 4. 生成回测报告的交易明细
 */
@Data
public class BacktestTradeRecord {
    /**
     * 交易标的代码
     * 例如：AAPL（美股苹果）、00700（港股腾讯）
     */
    private String symbol;

    /**
     * 交易类型
     * BUY - 买入
     * SELL - 卖出
     */
    private String type;

    /**
     * 交易执行时间
     * 使用 ISO 格式，例如：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 交易价格
     * 记录交易执行时的价格，用于计算交易成本和收益
     */
    private double price;

    /**
     * 交易数量
     * 记录买入或卖出的股票数量
     * 对于不同市场可能有不同的手数限制：
     * - 美股通常为100股的整数倍
     * - 港股根据不同股票有不同的手数规格
     */
    private int quantity;

    /**
     * 交易产生的盈亏
     * - 买入交易时为0
     * - 卖出交易时计算公式：(卖出价格 - 买入均价) * 数量 - 交易成本
     * 交易成本包括：
     * - 佣金（通常为交易金额的一定比例）
     * - 印花税（部分市场如港股在卖出时收取）
     * - 其他费用（如平台费、监管费等）
     */
    private double profit;
}