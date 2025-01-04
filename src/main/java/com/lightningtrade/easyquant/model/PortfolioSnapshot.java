package com.lightningtrade.easyquant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 投资组合快照模型类
 * 用于记录某一时刻投资组合的完整状态，包括现金、股票和总资产等信息
 * 主要功能：
 * 1. 记录投资组合的资产分布（现金、股票、总值）
 * 2. 保存所有持仓股票的详细信息
 * 3. 提供投资组合的完整状态视图
 * 
 * 该类通常用于：
 * - 回测系统记录每个时间点的投资组合状态
 * - 实时交易系统监控当前投资组合状态
 * - 风险管理系统分析投资组合风险
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshot {
    // 现金余额
    private double cash;
    // 股票市值（所有持仓股票的当前市值之和）
    private double stockValue;
    // 总资产（现金 + 股票市值）
    private double totalValue;
    // 持仓明细，key为股票代码，value为对应的持仓信息
    private Map<String, Position> positions;

    /**
     * 构造函数
     * 创建不包含持仓明细的投资组合快照
     * 
     * @param cash       现金余额
     * @param stockValue 股票市值
     * @param totalValue 总资产
     */
    public PortfolioSnapshot(double cash, double stockValue, double totalValue) {
        this.cash = cash;
        this.stockValue = stockValue;
        this.totalValue = totalValue;
    }
}