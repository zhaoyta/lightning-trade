package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.backtest.BacktestTradeRecord;
import com.tigerbrokers.stock.openapi.client.struct.enums.ActionType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Currency;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HKTradingService extends AbstractTradingService {

    private static final String MARKET = "hk";

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
    protected void processMarketData(MarketData data) {
        // 处理市场数据
        String symbol = data.getSymbol();
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
                double price = data.getClose();
                int lotSize = symbolConfig.getLotSize();

                Long orderId = tradeExecutor.placeMarketOrder(
                        symbol, lotSize, SecType.STK, Currency.HKD, ActionType.BUY);

                if (orderId != null) {
                    positions.put(symbol, lotSize);
                    logger.info("下单成功 - 股票: {}, 订单号: {}, 数量: {}, 方向: {}",
                            symbol, orderId, lotSize, "BUY");
                }
            }
        } else if ("SELL".equals(lastTrade.getType())) {
            if (position > 0) {
                Long orderId = tradeExecutor.placeMarketOrder(
                        symbol, position, SecType.STK, Currency.HKD, ActionType.SELL);

                if (orderId != null) {
                    positions.put(symbol, 0);
                    logger.info("下单成功 - 股票: {}, 订单号: {}, 数量: {}, 方向: {}",
                            symbol, orderId, position, "SELL");
                }
            }
        }
    }
}