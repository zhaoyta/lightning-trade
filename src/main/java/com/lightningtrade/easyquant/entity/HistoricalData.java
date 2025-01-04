package com.lightningtrade.easyquant.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 历史行情数据实体类
 * 用于存储和管理证券的历史K线数据，支持多种时间周期的行情数据存储
 * 主要内容：
 * 1. 证券基本信息（代码、时间）
 * 2. OHLCV数据（开盘价、最高价、最低价、收盘价、成交量）
 * 3. K线周期类型（日K、周K、月K、分钟K等）
 * 
 * 该类用于：
 * - 数据库存储和管理历史行情数据
 * - 为策略回测提供历史数据支持
 * - 为技术分析指标计算提供数据基础
 * - 市场分析和研究的数据支持
 */
@Data
@Entity
@Table(name = "historical_data")
public class HistoricalData {
    // 数据记录的唯一标识
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 证券代码
    private String symbol;

    // K线时间点
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
    private double volume;

    // K线周期类型
    // 可选值：
    // - day: 日K线
    // - week: 周K线
    // - month: 月K线
    // - year: 年K线
    // - min1: 1分钟K线
    // - min3: 3分钟K线
    // - min5: 5分钟K线
    // - min15: 15分钟K线
    // - min30: 30分钟K线
    // - min60: 60分钟K线
    // - min120: 120分钟K线
    // - min240: 240分钟K线
    private String kType;

    // 数据创建时间（数据入库时间）
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 数据保存前的预处理方法
     * 自动设置数据创建时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}