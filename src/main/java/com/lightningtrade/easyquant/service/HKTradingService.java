package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.model.Signal;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.tigerbrokers.stock.openapi.client.struct.enums.ActionType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Currency;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HKTradingService extends AbstractTradingService {

    @Autowired
    private TradingConfig tradingConfig;

    @Override
    protected boolean isEnabled() {
        return tradingConfig.getHk().isEnabled();
    }

    @Override
    protected List<TradingConfig.Symbol> getSymbols() {
        return tradingConfig.getHk().getSymbols();
    }

    @Override
    protected TradingConfig.Strategy getStrategyConfig() {
        return tradingConfig.getHk().getStrategy();
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

        Signal signal = strategy.execute(data);
        int position = positions.getOrDefault(symbol, 0);
        logger.info("策略执行结果 - 股票: {}, 信号: {}, 当前持仓: {}",
                symbol, signal, position);

        // 根据信号执行交易
        switch (signal) {
            case BUY:
                if (position <= 0) {
                    double price = data.getClose();
                    int lotSize = tradingConfig.getHk().getSymbols().stream()
                            .filter(s -> s.getSymbol().equals(symbol))
                            .findFirst()
                            .map(s -> s.getLotSize())
                            .orElse(100);

                    Long orderId = tradeExecutor.placeMarketOrder(
                            symbol, lotSize, SecType.STK, Currency.HKD, ActionType.BUY);

                    if (orderId != null) {
                        positions.put(symbol, position + lotSize);
                        logger.info("买入订单执行成功 - 股票: {}, 订单ID: {}, 数量: {}",
                                symbol, orderId, lotSize);
                    }
                }
                break;

            case SELL:
                if (position > 0) {
                    Long orderId = tradeExecutor.placeMarketOrder(
                            symbol, position, SecType.STK, Currency.HKD, ActionType.SELL);

                    if (orderId != null) {
                        positions.put(symbol, 0);
                        logger.info("卖出订单执行成功 - 股票: {}, 订单ID: {}, 数量: {}",
                                symbol, orderId, position);
                    }
                }
                break;

            default:
                logger.debug("无交易信号 - 股票: {}", symbol);
        }
    }
}