package com.lightningtrade.easyquant.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 回测结果模型类
 * 用于存储和展示策略回测的完整结果，包括绩效指标、交易记录和资产变化等信息
 * 主要内容：
 * 1. 回测基本信息（策略名称、回测区间、初始资金等）
 * 2. 绩效评估指标（总收益、最大回撤、胜率、夏普比率等）
 * 3. 详细的交易记录和资产变化历史
 * 
 * 该类用于：
 * - 策略绩效评估和分析
 * - 回测结果的展示和报告生成
 * - 不同策略的比较和优化
 */
@Data
public class BacktestResult {
    // 策略名称
    private String strategy;
    // 回测标的列表
    private List<String> symbols;
    // 回测开始时间
    private LocalDateTime startTime;
    // 回测结束时间
    private LocalDateTime endTime;
    // 初始资金
    private double initialCapital;
    // 最终资金
    private double finalCapital;
    // 总收益率 = (最终资金 - 初始资金) / 初始资金
    private double totalReturn;
    // 最大回撤，表示策略执行期间的最大亏损幅度
    private double maxDrawdown;
    // 总交易次数
    private int totalTrades;
    // 盈利交易次数
    private int winningTrades;
    // 胜率 = 盈利交易次数 / 总交易次数
    private double winRate;
    // 夏普比率，衡量风险调整后收益的指标
    // 计算公式：(策略收益率 - 无风险利率) / 收益率标准差
    private double sharpeRatio;
    // 详细交易记录列表，包含每笔交易的具体信息
    private List<BacktestTradeRecord> trades;
    // 投资组合历史记录，记录每个时间点的组合状态
    // key为时间点，value为该时间点的组合快照
    private Map<LocalDateTime, PortfolioSnapshot> portfolioHistory;
}