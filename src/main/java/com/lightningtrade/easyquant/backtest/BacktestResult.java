package com.lightningtrade.easyquant.backtest;

import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import lombok.Data;
import java.util.List;

/**
 * 回测结果类
 * 用于存储和展示策略回测的完整结果，包括绩效指标、交易记录和资金变化等信息
 * 
 * 主要用途：
 * 1. 记录回测的基本信息（股票、K线周期、资金等）
 * 2. 存储回测的绩效指标（收益率、最大回撤、夏普比率等）
 * 3. 保存交易记录和资金变化历史
 * 4. 为策略评估和优化提供数据支持
 */
@Data
public class BacktestResult {
    /**
     * 回测标的代码
     * 例如：AAPL（美股苹果）、00700（港股腾讯）
     */
    private String symbol;

    /**
     * K线周期类型
     * 可选值包括：
     * - 基础周期：day（日K）、week（周K）、month（月K）
     * - 分钟周期：min1、min3、min5、min15、min30、min60
     */
    private KType kType;

    /**
     * 初始资金
     * 回测开始时的起始资金，用于计算收益率和其他指标
     */
    private double initialCapital;

    /**
     * 最终资金
     * 回测结束时的资金，包含所有交易产生的盈亏和成本
     */
    private double finalCapital;

    /**
     * 总收益率
     * 计算公式：(最终资金 - 初始资金) / 初始资金
     * 反映策略在整个回测期间的盈利能力
     */
    private double totalReturn;

    /**
     * 最大回撤
     * 在回测期间任意时间点上的最大亏损百分比
     * 计算方法：(谷值 - 峰值) / 峰值
     * 用于衡量策略的风险控制能力
     */
    private double maxDrawdown;

    /**
     * 夏普比率
     * 计算公式：(策略收益率 - 无风险利率) / 收益率标准差
     * 用于衡量策略的风险调整后收益
     * - 大于1：较好
     * - 大于2：很好
     * - 小于0：不够理想
     */
    private double sharpeRatio;

    /**
     * 胜率
     * 计算公式：盈利交易次数 / 总交易次数
     * 反映策略的交易准确性
     */
    private double winRate;

    /**
     * 交易记录列表
     * 包含回测期间所有的交易明细
     * 每条记录包含交易时间、类型、价格、数量、盈亏等信息
     */
    private List<BacktestTradeRecord> trades;

    /**
     * 权益曲线
     * 记录回测过程中每个时间点的账户总价值变化
     * 用于：
     * 1. 绘制权益曲线图
     * 2. 计算最大回撤
     * 3. 分析策略的稳定性
     */
    private List<Double> equityCurve;
}