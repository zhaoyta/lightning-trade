package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.execution.TradeExecutor;
import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.strategy.StrategyFactory;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractTradingService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected DataService dataService;

    @Autowired
    protected TradeExecutor tradeExecutor;

    @Autowired
    protected StrategyFactory strategyFactory;

    protected final ConcurrentHashMap<String, TradingStrategy> strategies = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Integer> positions = new ConcurrentHashMap<>();

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

    protected abstract void processMarketData(MarketData data);
}