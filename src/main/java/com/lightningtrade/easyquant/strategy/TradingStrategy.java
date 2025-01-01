package com.lightningtrade.easyquant.strategy;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Signal;

/**
 * 交易策略接口
 */
public interface TradingStrategy {
    /**
     * 执行策略
     * 
     * @param data 市场数据
     * @return 交易信号
     */
    Signal execute(MarketData data);

    /**
     * 初始化策略
     */
    default void initialize() {
        // 默认空实现
    }

    /**
     * 重置策略状态
     */
    default void reset() {
        // 默认空实现
    }
}