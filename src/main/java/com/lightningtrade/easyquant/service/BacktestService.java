package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.backtest.BacktestEngine;
import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.strategy.StrategyFactory;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import com.lightningtrade.easyquant.model.MarketData;

@Service
public class BacktestService {
    private static final Logger logger = LoggerFactory.getLogger(BacktestService.class);

    @Autowired
    private DataService dataService;

    @Autowired
    private StrategyFactory strategyFactory;

    @Autowired
    private BacktestEngine backtestEngine;

    public BacktestResult runBacktest(String symbol, TradingConfig.Strategy strategyConfig,
            LocalDateTime startTime, LocalDateTime endTime, double initialCapital) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("交易品种不能为空");
        }

        logger.info("开始回测 - 策略: {}, 股票: {}, 初始资金: {}",
                strategyConfig.getType(), symbol, initialCapital);

        try {
            // 获取历史数据
            List<MarketData> historicalData = dataService.getHistoricalData(symbol, strategyConfig.getMarket(),
                    startTime, endTime, KType.valueOf(strategyConfig.getKType()));
            if (historicalData.isEmpty()) {
                logger.warn("未获取到历史数据 - 股票: {}", symbol);
                return createEmptyResult(Collections.singletonList(symbol), initialCapital);
            }

            // 创建策略实例
            TradingStrategy strategy = strategyFactory.createStrategy(strategyConfig);

            // 执行回测
            BacktestResult result = backtestEngine.runBacktest(symbol, historicalData, strategy, initialCapital,
                    KType.valueOf(strategyConfig.getKType()));

            logger.info("回测完成 - 股票: {}, 收益率: {}, 最大回撤: {}",
                    symbol, result.getTotalReturn(), result.getMaxDrawdown());

            return result;

        } catch (Exception e) {
            logger.error("回测过程发生错误 - 股票: " + symbol, e);
            return createEmptyResult(Collections.singletonList(symbol), initialCapital);
        }
    }

    private BacktestResult createEmptyResult(List<String> symbols, double initialCapital) {
        BacktestResult result = new BacktestResult();
        result.setInitialCapital(initialCapital);
        result.setFinalCapital(initialCapital);
        result.setTotalReturn(0);
        result.setMaxDrawdown(0);
        result.setSharpeRatio(0);
        result.setWinRate(0);
        result.setTrades(Collections.emptyList());
        result.setEquityCurve(Collections.singletonList(initialCapital));
        return result;
    }
}