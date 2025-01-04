package com.lightningtrade.easyquant.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightningtrade.easyquant.backtest.BacktestResult;
import com.lightningtrade.easyquant.config.TradingConfig;
import com.lightningtrade.easyquant.service.BacktestService;
import com.lightningtrade.easyquant.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TradeCalendar;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/backtest")
public class BacktestController {
    private static final Logger logger = LoggerFactory.getLogger(BacktestController.class);

    @Autowired
    private BacktestService backtestService;

    @Autowired
    private DataService dataService;

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
            List<Map<String, Object>> klineData = dataService.getHistoricalData(symbol, market, startTime, endTime,
                    KType.valueOf(kType));
            return ResponseEntity.ok(klineData);
        } catch (Exception e) {
            logger.error("获取K线数据失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "获取K线数据失败: " + e.getMessage()));
        }
    }

    public static class BacktestRequest {
        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("strategyType")
        private String strategyType;

        @JsonProperty("market")
        private String market;

        @JsonProperty("initialCapital")
        private double initialCapital;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("startTime")
        private LocalDateTime startTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("endTime")
        private LocalDateTime endTime;

        @JsonProperty("shortPeriod")
        private Integer shortPeriod;

        @JsonProperty("longPeriod")
        private Integer longPeriod;

        @JsonProperty("signalPeriod")
        private Integer signalPeriod;

        @JsonProperty("oversoldThreshold")
        private Double oversoldThreshold;

        @JsonProperty("overboughtThreshold")
        private Double overboughtThreshold;

        @JsonProperty("kValue")
        private Double kValue;

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
