package com.lightningtrade.easyquant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 回测交易记录模型类
 * 用于记录回测过程中每笔交易的详细信息
 * 主要内容：
 * 1. 交易基本信息（股票代码、时间、类型）
 * 2. 交易细节（价格、数量）
 * 3. 交易结果（盈亏）
 * 
 * 该类用于：
 * - 记录和追踪每笔交易的执行情况
 * - 分析策略的交易行为和特征
 * - 计算交易相关的统计指标
 * - 生成交易报告和明细
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacktestTradeRecord {
    // 交易标的代码
    private String symbol;
    // 交易执行时间
    private LocalDateTime time;
    // 交易类型（买入/卖出）
    private String type;
    // 交易价格
    private double price;
    // 交易数量（股数）
    private int quantity;
    // 交易产生的盈亏
    // 买入时为0
    // 卖出时 = (卖出价格 - 买入均价) * 数量
    private double profit;
}