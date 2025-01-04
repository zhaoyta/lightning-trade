package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.backtest.BacktestTradeRecord;
import com.tigerbrokers.stock.openapi.client.struct.enums.ActionType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Currency;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class USTradingService extends AbstractTradingService {

    private static final String MARKET = "us";

    @Autowired
    private TradingConfig tradingConfig;

    @Override
    public boolean isEnabled() {
        return tradingConfig != null
                && tradingConfig.getMarkets() != null
                && tradingConfig.getMarkets().containsKey(MARKET)
                && tradingConfig.getMarkets().get(MARKET).isEnabled();
    }

    @Override
    public List<TradingConfig.Symbol> getSymbols() {
        if (!isEnabled()) {
            return List.of();
        }
        return tradingConfig.getMarkets().get(MARKET).getSymbols().values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    protected TradingConfig.Strategy getStrategyConfig() {
        if (!isEnabled()) {
            return null;
        }
        return tradingConfig.getMarkets().get(MARKET).getStrategy();
    }

    @Override
    protected void processMarketData(Map<String, Object> data) {
        // 处理市场数据
        String symbol = (String) data.get("symbol");
        TradingStrategy strategy = strategies.get(symbol);
        if (strategy == null) {
            logger.warn("未找到策略配置 - 股票: {}", symbol);
            return;
        }

        BacktestResult result = strategy.backtest(symbol, List.of(data), 1000000, KType.min1);
        List<BacktestTradeRecord> trades = result.getTrades();
        if (trades == null || trades.isEmpty()) {
            logger.debug("无交易信号 - 股票: {}", symbol);
            return;
        }

        BacktestTradeRecord lastTrade = trades.get(trades.size() - 1);
        int position = positions.getOrDefault(symbol, 0);
        logger.info("策略执行结果 - 股票: {}, 信号: {}, 当前持仓: {}",
                symbol, lastTrade.getType(), position);

        // 获取交易配置
        TradingConfig.Symbol symbolConfig = tradingConfig.getMarkets().get(MARKET)
                .getSymbols().get(symbol);

        if (symbolConfig == null) {
            logger.error("未找到股票配置 - 股票: {}", symbol);
            return;
        }

        // 根据信号执行交易
        if ("BUY".equals(lastTrade.getType())) {
            if (position <= 0) {
                double price = (double) data.get("close");
                int lotSize = symbolConfig.getLotSize();

                Long orderId = tradeExecutor.placeMarketOrder(
                        symbol, lotSize, SecType.STK, Currency.USD, ActionType.BUY);

                if (orderId != null) {
                    positions.put(symbol, position + lotSize);
                    logger.info("买入订单执行成功 - 股票: {}, 订单ID: {}, 数量: {}",
                            symbol, orderId, lotSize);
                }
            }
        } else if ("SELL".equals(lastTrade.getType())) {
            if (position > 0) {
                Long orderId = tradeExecutor.placeMarketOrder(
                        symbol, position, SecType.STK, Currency.USD, ActionType.SELL);

                if (orderId != null) {
                    positions.put(symbol, 0);
                    logger.info("卖出订单执行成功 - 股票: {}, 订单ID: {}, 数量: {}",
                            symbol, orderId, position);
                }
            }
        }
    }
}