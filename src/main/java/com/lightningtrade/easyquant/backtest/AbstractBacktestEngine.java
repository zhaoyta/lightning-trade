package com.lightningtrade.easyquant.backtest;

import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Position;
import com.lightningtrade.easyquant.model.Signal;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBacktestEngine {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Getter
    protected final Map<String, Position> positions = new HashMap<>();
    protected final Map<String, TradingStrategy> strategies;
    protected final double initialCapital;
    protected double cash;
    protected double totalValue;

    public AbstractBacktestEngine(Map<String, TradingStrategy> strategies, double initialCapital) {
        this.strategies = strategies;
        this.initialCapital = initialCapital;
        this.cash = initialCapital;
        this.totalValue = initialCapital;
    }

    protected abstract int getLotSize(String symbol);

    protected abstract double getTransactionCost(String symbol, int quantity, double price);

    public void run(Map<String, List<MarketData>> historicalData) {
        logger.info("开始回测 - 初始资金: {}", String.format("%.2f", initialCapital));

        // 初始化策略和持仓
        for (String symbol : strategies.keySet()) {
            strategies.get(symbol).initialize();
            positions.put(symbol, new Position(symbol));
        }

        // 获取所有数据的时间点
        List<Long> timestamps = new ArrayList<>();
        for (List<MarketData> data : historicalData.values()) {
            for (MarketData md : data) {
                long timestamp = md.getDateTime().toEpochSecond(java.time.ZoneOffset.UTC);
                if (!timestamps.contains(timestamp)) {
                    timestamps.add(timestamp);
                }
            }
        }
        timestamps.sort(Long::compareTo);

        // 按时间顺序遍历每个数据点
        for (Long timestamp : timestamps) {
            for (Map.Entry<String, List<MarketData>> entry : historicalData.entrySet()) {
                String symbol = entry.getKey();
                List<MarketData> data = entry.getValue();

                // 找到当前时间点的数据
                MarketData currentData = data.stream()
                        .filter(md -> md.getDateTime().toEpochSecond(java.time.ZoneOffset.UTC) == timestamp)
                        .findFirst()
                        .orElse(null);

                if (currentData != null) {
                    processMarketData(symbol, currentData);
                }
            }
        }

        // 输出回测结果
        printResults();
    }

    protected void processMarketData(String symbol, MarketData data) {
        TradingStrategy strategy = strategies.get(symbol);
        Position position = positions.get(symbol);

        // 更新持仓市值
        position.updateMarketValue(data.getClose());

        // 执行策略
        Signal signal = strategy.execute(data);
        logger.debug("策略执行结果 - 股票: {}, 时间: {}, 信号: {}",
                symbol, data.getDateTime(), signal);

        // 根据信号执行交易
        switch (signal) {
            case BUY:
                if (!position.isHolding()) {
                    int tradeQuantity = calculateTradeQuantity(symbol, data.getClose());
                    if (tradeQuantity > 0) {
                        double totalCost = data.getClose() * tradeQuantity +
                                getTransactionCost(symbol, tradeQuantity, data.getClose());
                        if (totalCost <= cash) {
                            executeBuy(symbol, tradeQuantity, data.getClose());
                        } else {
                            logger.debug("资金不足，无法买入 - 所需资金: {}, 当前现金: {}",
                                    String.format("%.2f", totalCost), String.format("%.2f", cash));
                        }
                    }
                }
                break;
            case SELL:
                if (position.isHolding()) {
                    executeSell(symbol, position.getQuantity(), data.getClose());
                }
                break;
            case HOLD:
            case NONE:
                // 更新持仓市值
                position.updateMarketValue(data.getClose());
                break;
        }

        // 更新总市值
        updatePortfolioValue();
    }

    protected int calculateTradeQuantity(String symbol, double price) {
        int lotSize = getLotSize(symbol);
        // 预留10%作为交易成本和缓冲
        double availableCash = cash * 0.9;
        // 计算每手的总成本（包括交易费用）
        double lotCost = price * lotSize;
        double lotTransactionCost = getTransactionCost(symbol, lotSize, price);
        double totalLotCost = lotCost + lotTransactionCost;

        // 计算最大可买手数
        int maxLots = (int) (availableCash / totalLotCost);
        return maxLots * lotSize;
    }

    protected void executeBuy(String symbol, int quantity, double price) {
        double cost = price * quantity;
        double transactionCost = getTransactionCost(symbol, quantity, price);
        double totalCost = cost + transactionCost;

        if (totalCost <= cash) {
            Position position = positions.get(symbol);
            position.buy(price, quantity);
            cash -= totalCost;

            logger.info("买入 - 股票: {}, 数量: {}, 价格: {}, 交易成本: {}",
                    symbol, quantity, String.format("%.4f", price), String.format("%.2f", transactionCost));
        }
    }

    protected void executeSell(String symbol, int quantity, double price) {
        double revenue = price * quantity;
        double transactionCost = getTransactionCost(symbol, quantity, price);
        double netRevenue = revenue - transactionCost;

        Position position = positions.get(symbol);
        // 计算本次交易的盈亏
        double tradePnL = (price - position.getEntryPrice()) * quantity - transactionCost;
        position.sell(price);
        cash += netRevenue;

        logger.info("卖出 - 股票: {}, 数量: {}, 价格: {}, 交易成本: {}, 本次盈亏: {}",
                symbol, quantity, String.format("%.4f", price),
                String.format("%.2f", transactionCost), String.format("%.2f", tradePnL));
    }

    protected void updatePortfolioValue() {
        totalValue = cash;
        for (Position position : positions.values()) {
            totalValue += position.getMarketValue();
        }
    }

    protected void printResults() {
        double totalPnL = totalValue - initialCapital;
        double returnRate = (totalValue / initialCapital - 1) * 100;

        logger.info("回测结束 ====================");
        logger.info("初始资金: {}", String.format("%.2f", initialCapital));
        logger.info("最终资金: {}", String.format("%.2f", totalValue));
        logger.info("总盈亏: {}", String.format("%.2f", totalPnL));
        logger.info("收益率: {}%", String.format("%.2f", returnRate));
        logger.info("当前持仓:");
        positions.forEach((symbol, position) -> {
            if (position.isHolding()) {
                logger.info("  {} - 数量: {}, 成本价: {}, 市值: {}, 浮动盈亏: {}",
                        symbol,
                        position.getQuantity(),
                        String.format("%.4f", position.getEntryPrice()),
                        String.format("%.2f", position.getMarketValue()),
                        String.format("%.2f", position.getUnrealizedPnL()));
            }
        });
    }
}