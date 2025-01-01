package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Position;
import com.lightningtrade.easyquant.model.Signal;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 回测引擎
 * 用于在历史数据上测试交易策略的表现
 */
@Data
public class BacktestEngine {
    private static final Logger logger = LoggerFactory.getLogger(BacktestEngine.class);

    /** 交易策略 */
    private TradingStrategy strategy;
    /** 初始资金 */
    private double initialCapital;
    /** 当前持仓 */
    private Position position;
    /** 历史数据 */
    private List<MarketData> historicalData;

    /**
     * 构造函数
     * 
     * @param strategy       交易策略
     * @param initialCapital 初始资金
     */
    public BacktestEngine(TradingStrategy strategy, double initialCapital) {
        this.strategy = strategy;
        this.initialCapital = initialCapital;
        this.position = new Position();
        logger.info("初始化回测引擎 - 初始资金: {}", initialCapital);
    }

    /**
     * 运行回测
     * 
     * 1. 首先初始化交易策略
     * 2. 遍历历史数据,对每个数据点:
     * - 执行策略获取交易信号
     * - 根据信号执行买入/卖出操作
     * - 更新持仓状态
     * 3. 最终可以通过position对象获取回测结果
     */
    public void run() {
        logger.info("开始回测 - 数据点数量: {}", historicalData.size());
        strategy.initialize();

        for (MarketData data : historicalData) {
            Signal signal = strategy.execute(data);
            logger.debug("处理数据点 - 时间: {}, 价格: {}, 信号: {}",
                    data.getDateTime(), data.getClose(), signal);

            switch (signal) {
                case BUY:
                    if (!position.isHolding()) {
                        int quantity = (int) (initialCapital / data.getClose());
                        position.buy(data.getClose(), quantity);
                        logger.info("执行买入 - 价格: {}, 数量: {}", data.getClose(), quantity);
                    }
                    break;
                case SELL:
                    if (position.isHolding()) {
                        logger.info("执行卖出 - 价格: {}, 数量: {}, 盈亏: {}",
                                data.getClose(), position.getQuantity(),
                                (data.getClose() - position.getEntryPrice()) * position.getQuantity());
                        position.sell(data.getClose());
                    }
                    break;
                case HOLD:
                    if (position.isHolding()) {
                        // 更新持仓市值
                        position.updateMarketValue(data.getClose());
                        logger.debug("保持持仓 - 当前市值: {}, 浮动盈亏: {}",
                                position.getMarketValue(), position.getUnrealizedPnL());
                    }
                    break;
            }
        }

        logger.info("回测完成 - 最终持仓: {}, 盈亏: {}", position.getQuantity(), position.getPnL());
    }
}