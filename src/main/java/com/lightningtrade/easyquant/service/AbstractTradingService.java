package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.execution.TradeExecutor;
import com.lightningtrade.easyquant.strategy.StrategyFactory;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.backtest.BacktestTradeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractTradingService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected DataService dataService;

    @Autowired
    protected TradeExecutor tradeExecutor;

    @Autowired
    protected StrategyFactory strategyFactory;

    protected final Map<String, TradingStrategy> strategies = new ConcurrentHashMap<>();
    protected final Map<String, Integer> positions = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (!isEnabled()) {
            logger.info("交易服务未启用");
            return;
        }

        // 初始化策略
        TradingConfig.Strategy strategyConfig = getStrategyConfig();
        if (strategyConfig == null) {
            logger.warn("未找到策略配置");
            return;
        }

        // 初始化交易品种
        List<TradingConfig.Symbol> symbols = getSymbols();
        if (symbols.isEmpty()) {
            logger.warn("未找到交易品种配置");
            return;
        }

        // 为每个交易品种创建策略实例
        TradingStrategy strategy = strategyFactory.createStrategy(strategyConfig);
        for (TradingConfig.Symbol symbol : symbols) {
            strategies.put(symbol.getCode(), strategy);
            positions.put(symbol.getCode(), 0);
        }

        logger.info("初始化交易服务 - 策略: {}, 交易品种数量: {}",
                strategyConfig.getType(), symbols.size());
    }

    public abstract boolean isEnabled();

    public abstract List<TradingConfig.Symbol> getSymbols();

    protected abstract TradingConfig.Strategy getStrategyConfig();

    protected abstract void processMarketData(Map<String, Object> data);

    protected Map<String, Object> convertToMarketData(Map<String, Object> histData) {
        Map<String, Object> md = new HashMap<>();
        md.put("symbol", histData.get("symbol"));
        md.put("time", histData.get("time"));
        md.put("open", histData.get("open"));
        md.put("high", histData.get("high"));
        md.put("low", histData.get("low"));
        md.put("close", histData.get("close"));
        md.put("volume", histData.get("volume"));
        return md;
    }
}