package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.backtest.*;
import com.lightningtrade.easyquant.model.MarketData;
import com.lightningtrade.easyquant.strategy.MACrossStrategy;
import com.lightningtrade.easyquant.strategy.TradingStrategy;
import com.lightningtrade.easyquant.entity.HistoricalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BacktestService implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(BacktestService.class);

    @Value("${backtest.enabled:false}")
    private boolean backtestEnabled;

    @Autowired
    private DataService dataService;

    @Autowired
    private BacktestConfig backtestConfig;

    @Override
    public void run(String... args) {
        if (!backtestEnabled) {
            logger.info("回测功能未启用，跳过回测执行");
            return;
        }

        logger.info("开始运行回测策略...");

        // 根据配置创建策略实例
        Map<String, TradingStrategy> strategies = new HashMap<>();
        for (Map.Entry<String, BacktestConfig.SymbolConfig> entry : backtestConfig.getSymbols().entrySet()) {
            strategies.put(entry.getKey(), new MACrossStrategy(5, 20));
        }

        // 获取历史数据
        Map<String, List<MarketData>> historicalData = loadHistoricalData();

        if (historicalData.isEmpty()) {
            logger.warn("没有找到历史数据，回测终止");
            return;
        }

        // 创建回测引擎
        AbstractBacktestEngine engine;
        if ("US".equals(backtestConfig.getMarket())) {
            engine = new USBacktestEngine(strategies, backtestConfig.getInitialCapital());
        } else {
            engine = new HKBacktestEngine(strategies, backtestConfig.getInitialCapital());
        }

        // 运行回测
        engine.run(historicalData);
    }

    private Map<String, List<MarketData>> loadHistoricalData() {
        Map<String, List<MarketData>> data = new HashMap<>();

        // 获取每个股票的历史数据
        for (String symbol : backtestConfig.getSymbols().keySet()) {
            List<HistoricalData> histData = dataService.getHistoricalData(symbol,
                    backtestConfig.getStartTime(), backtestConfig.getEndTime());

            if (histData == null || histData.isEmpty()) {
                logger.warn("未找到股票{}的历史数据", symbol);
                continue;
            }

            // 转换数据格式
            List<MarketData> symbolData = new ArrayList<>();
            for (HistoricalData hist : histData) {
                if (hist != null) {
                    MarketData md = new MarketData();
                    md.setSymbol(hist.getSymbol());
                    md.setDateTime(hist.getDateTime());
                    md.setOpen(hist.getOpen());
                    md.setHigh(hist.getHigh());
                    md.setLow(hist.getLow());
                    md.setClose(hist.getClose());
                    md.setVolume((long) hist.getVolume());
                    symbolData.add(md);
                }
            }

            if (!symbolData.isEmpty()) {
                data.put(symbol, symbolData);
            }
        }

        return data;
    }
}