package com.lightningtrade.easyquant.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.service.BacktestService;
import com.lightningtrade.easyquant.service.DataService;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TradeCalendar;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.lightningtrade.easyquant.model.MarketData;

/**
 * 回测控制器
 * 提供策略回测相关的REST API接口，包括执行回测、获取交易日历和K线数据等功能
 * 主要功能：
 * 1. 执行策略回测
 * 2. 获取交易日历
 * 3. 获取历史K线数据
 * 
 * API端点：
 * - POST /api/backtest/run：执行策略回测
 * - GET /api/backtest/tradingDays：获取交易日历
 * - GET /api/backtest/kline：获取K线数据
 */
@RestController
@RequestMapping("/api/backtest")
public class BacktestController {
    private static final Logger logger = LoggerFactory.getLogger(BacktestController.class);

    @Autowired
    private BacktestService backtestService;

    @Autowired
    private DataService dataService;

    /**
     * 执行策略回测
     * 接收回测参数，执行回测，并返回回测结果
     * 
     * @param request 回测请求参数，包含策略类型、股票代码、回测区间等信息
     * @return 回测结果，包含收益率、最大回撤等绩效指标
     */
    @PostMapping("/run")
    public ResponseEntity<BacktestResult> runBacktest(@RequestBody BacktestRequest request) {
        try {
            // 打印完整的请求体
            logger.info("接收到的原始请求体: {}", request);

            // 参数校验
            if (request == null) {
                logger.error("请求参数为空");
                return ResponseEntity.badRequest().build();
            }

            if (request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
                logger.error("股票代码不能为空");
                return ResponseEntity.badRequest().build();
            }

            if (request.getStrategyType() == null || request.getStrategyType().trim().isEmpty()) {
                logger.error("策略类型不能为空");
                return ResponseEntity.badRequest().build();
            }

            if (request.getStartTime() == null || request.getEndTime() == null) {
                logger.error("开始时间和结束时间不能为空");
                return ResponseEntity.badRequest().build();
            }

            if (request.getInitialCapital() <= 0) {
                logger.error("初始资金必须大于0");
                return ResponseEntity.badRequest().build();
            }

            if (request.getKType() == null || request.getKType().trim().isEmpty()) {
                request.setKType("day"); // 如果没有传入K线周期，使用默认值
            }

            logger.info("收到回测请求 - 股票: {}, 市场: {}, 策略: {}, K线周期: {}, 初始资金: {}",
                    request.getSymbol(), request.getMarket(), request.getStrategyType(), request.getKType(),
                    request.getInitialCapital());

            // 创建策略配置
            TradingConfig.Strategy strategyConfig = new TradingConfig.Strategy();
            strategyConfig.setType(request.getStrategyType());
            strategyConfig.setKType(request.getKType());
            strategyConfig.setMarket(request.getMarket());

            // 根据策略类型设置相应的参数
            if (request.getShortPeriod() != null) {
                strategyConfig.setShortPeriod(request.getShortPeriod());
            }
            if (request.getLongPeriod() != null) {
                strategyConfig.setLongPeriod(request.getLongPeriod());
            }
            if (request.getSignalPeriod() != null) {
                strategyConfig.setSignalPeriod(request.getSignalPeriod());
            }
            if (request.getOversoldThreshold() != null) {
                strategyConfig.setOversoldThreshold(request.getOversoldThreshold());
            }
            if (request.getOverboughtThreshold() != null) {
                strategyConfig.setOverboughtThreshold(request.getOverboughtThreshold());
            }
            if (request.getKValue() != null) {
                strategyConfig.setKValue(request.getKValue());
            }

            BacktestResult result = backtestService.runBacktest(
                    request.getSymbol(),
                    strategyConfig,
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getInitialCapital());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("回测执行失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取交易日历
     * 根据市场和日期范围获取交易日列表
     * 
     * @param market    市场代码（如：US-美股，HK-港股）
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate   结束日期（格式：yyyy-MM-dd）
     * @return 交易日期列表
     */
    @GetMapping("/tradingDays")
    public ResponseEntity<List<String>> getTradingDays(
            @RequestParam String market,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime startTime = LocalDate.parse(startDate, formatter).atStartOfDay();
            LocalDateTime endTime = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);

            List<TradeCalendar> tradingDays = dataService.getTradingDays(market, startTime, endTime);

            // 转换为前端需要的格式
            List<String> tradingDates = tradingDays.stream()
                    .map(TradeCalendar::getDate)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tradingDates);
        } catch (Exception e) {
            logger.error("获取交易日历失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取K线数据
     * 根据股票代码、时间范围和K线类型获取历史K线数据
     * 
     * @param symbol    股票代码
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate   结束日期（格式：yyyy-MM-dd）
     * @param kType     K线类型（默认：day）
     * @param market    市场代码（默认：US）
     * @return K线数据列表
     */
    @GetMapping("/kline")
    public ResponseEntity<?> getKlineData(
            @RequestParam String symbol,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "day") String kType,
            @RequestParam(defaultValue = "US") String market) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime endTime = LocalDateTime.parse(endDate + "T23:59:59");
            List<MarketData> klineData = dataService.getHistoricalData(symbol, market, startTime, endTime,
                    KType.valueOf(kType));
            return ResponseEntity.ok(klineData);
        } catch (Exception e) {
            logger.error("获取K线数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "获取K线数据失败: " + e.getMessage()));
        }
    }

    /**
     * 回测请求参数类
     * 用于封装回测接口的请求参数
     */
    public static class BacktestRequest {
        // 股票代码
        @JsonProperty("symbol")
        private String symbol;

        // 策略类型
        @JsonProperty("strategyType")
        private String strategyType;

        // 市场代码（US-美股，HK-港股）
        @JsonProperty("market")
        private String market;

        // 初始资金
        @JsonProperty("initialCapital")
        private double initialCapital;

        // 回测开始时间
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("startTime")
        private LocalDateTime startTime;

        // 回测结束时间
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("endTime")
        private LocalDateTime endTime;

        // 短期均线周期（用于MA策略）
        @JsonProperty("shortPeriod")
        private Integer shortPeriod;

        // 长期均线周期（用于MA策略）
        @JsonProperty("longPeriod")
        private Integer longPeriod;

        // MACD信号线周期
        @JsonProperty("signalPeriod")
        private Integer signalPeriod;

        // RSI超卖阈值
        @JsonProperty("oversoldThreshold")
        private Double oversoldThreshold;

        // RSI超买阈值
        @JsonProperty("overboughtThreshold")
        private Double overboughtThreshold;

        // 布林带参数
        @JsonProperty("kValue")
        private Double kValue;

        // K线周期类型
        @JsonProperty("kType")
        private String kType;

        // Getters and Setters
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getStrategyType() {
            return strategyType;
        }

        public void setStrategyType(String strategyType) {
            this.strategyType = strategyType;
        }

        public double getInitialCapital() {
            return initialCapital;
        }

        public void setInitialCapital(double initialCapital) {
            this.initialCapital = initialCapital;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Integer getShortPeriod() {
            return shortPeriod;
        }

        public void setShortPeriod(Integer shortPeriod) {
            this.shortPeriod = shortPeriod;
        }

        public Integer getLongPeriod() {
            return longPeriod;
        }

        public void setLongPeriod(Integer longPeriod) {
            this.longPeriod = longPeriod;
        }

        public Integer getSignalPeriod() {
            return signalPeriod;
        }

        public void setSignalPeriod(Integer signalPeriod) {
            this.signalPeriod = signalPeriod;
        }

        public Double getOversoldThreshold() {
            return oversoldThreshold;
        }

        public void setOversoldThreshold(Double oversoldThreshold) {
            this.oversoldThreshold = oversoldThreshold;
        }

        public Double getOverboughtThreshold() {
            return overboughtThreshold;
        }

        public void setOverboughtThreshold(Double overboughtThreshold) {
            this.overboughtThreshold = overboughtThreshold;
        }

        public Double getKValue() {
            return kValue;
        }

        public void setKValue(Double kValue) {
            this.kValue = kValue;
        }

        public String getKType() {
            return kType;
        }

        public void setKType(String kType) {
            logger.info("设置 kType: {}", kType);
            this.kType = kType;
        }

        public String getMarket() {
            return market;
        }

        public void setMarket(String market) {
            this.market = market;
        }

        @Override
        public String toString() {
            return "BacktestRequest{" +
                    "symbol='" + symbol + '\'' +
                    ", strategyType='" + strategyType + '\'' +
                    ", market='" + market + '\'' +
                    ", initialCapital=" + initialCapital +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", shortPeriod=" + shortPeriod +
                    ", longPeriod=" + longPeriod +
                    ", signalPeriod=" + signalPeriod +
                    ", oversoldThreshold=" + oversoldThreshold +
                    ", overboughtThreshold=" + overboughtThreshold +
                    ", kValue=" + kValue +
                    ", kType='" + kType + '\'' +
                    '}';
        }
    }
}
