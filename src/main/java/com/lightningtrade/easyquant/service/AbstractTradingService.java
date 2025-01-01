package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.entity.HistoricalData;
import com.lightningtrade.easyquant.execution.TradeExecutor;
import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.strategy.StrategyFactory;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTradingService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected DataService dataService;

    @Autowired
    protected TradeExecutor tradeExecutor;

    @Autowired
    protected StrategyFactory strategyFactory;

    protected final Map<String, TradingStrategy> strategies = new HashMap<>();
    protected final Map<String, Integer> positions = new HashMap<>();

    /**
     * 检查服务是否启用
     */
    protected abstract boolean isEnabled();

    /**
     * 获取交易标的列表
     */
    protected abstract List<TradingConfig.Symbol> getSymbols();

    /**
     * 获取策略配置
     */
    protected abstract TradingConfig.Strategy getStrategyConfig();

    /**
     * 处理市场数据
     */
    protected abstract void processMarketData(MarketData data);

    @PostConstruct
    public void init() {
        if (!isEnabled()) {
            logger.info("{} 交易服务未启用", getClass().getSimpleName());
            return;
        }

        // 初始化策略
        TradingConfig.Strategy strategyConfig = getStrategyConfig();
        if (strategyConfig == null) {
            logger.error("策略配置为空");
            return;
        }

        for (TradingConfig.Symbol symbol : getSymbols()) {
            try {
                TradingStrategy strategy = strategyFactory.createStrategy(strategyConfig);
                if (strategy != null) {
                    strategies.put(symbol.getSymbol(), strategy);
                    strategy.initialize();
                    logger.info("初始化策略成功 - 股票: {}, 策略类型: {}",
                            symbol.getSymbol(), strategy.getClass().getSimpleName());
                } else {
                    logger.error("创建策略失败 - 股票: {}", symbol.getSymbol());
                }
            } catch (Exception e) {
                logger.error("创建策略异常 - 股票: {}, 错误: {}", symbol.getSymbol(), e.getMessage());
            }
        }

        // 获取初始数据
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);

        for (TradingConfig.Symbol symbol : getSymbols()) {
            List<HistoricalData> histData = dataService.getHistoricalData(
                    symbol.getSymbol(), startTime, endTime);

            for (HistoricalData data : histData) {
                MarketData marketData = convertToMarketData(data);
                processMarketData(marketData);
            }
        }
    }

    protected MarketData convertToMarketData(HistoricalData histData) {
        MarketData md = new MarketData();
        md.setSymbol(histData.getSymbol());
        md.setDateTime(histData.getDateTime());
        md.setOpen(histData.getOpen());
        md.setHigh(histData.getHigh());
        md.setLow(histData.getLow());
        md.setClose(histData.getClose());
        md.setVolume((long) histData.getVolume());
        return md;
    }
}