package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.entity.HistoricalData;
import com.lightningtrade.easyquant.repository.HistoricalDataRepository;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.request.quote.QuoteKlineRequest;
import com.tigerbrokers.stock.openapi.client.https.request.quote.QuoteTradeCalendarRequest;
import com.tigerbrokers.stock.openapi.client.https.response.quote.QuoteKlineResponse;
import com.tigerbrokers.stock.openapi.client.https.response.quote.QuoteTradeCalendarResponse;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import com.tigerbrokers.stock.openapi.client.struct.enums.TimeZoneId;
import com.tigerbrokers.stock.openapi.client.util.DateUtils;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.KlineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.KlinePoint;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TradeCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tigerbrokers.stock.openapi.client.struct.enums.Market;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class DataService {
    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Autowired
    private HistoricalDataRepository historicalDataRepository;

    @Autowired
    private TigerHttpClient tigerClient;

    /**
     * 获取交易日历
     */
    public List<TradeCalendar> getTradingDays(String market, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 市场代码转大写
            market = market.toUpperCase();
            Market marketEnum = Market.valueOf(market);

            QuoteTradeCalendarRequest request = QuoteTradeCalendarRequest.newRequest(
                    marketEnum,
                    DateUtils.printDate(startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            TimeZoneId.NewYork),
                    DateUtils.printDate(endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            TimeZoneId.NewYork));

            QuoteTradeCalendarResponse response = tigerClient.execute(request);
            if (!response.isSuccess()) {
                logger.error("获取交易日历失败 - 市场: {}, 错误码: {}, 错误信息: {}",
                        market, response.getCode(), response.getMessage());
                return new ArrayList<>();
            }

            return response.getItems();
        } catch (Exception e) {
            logger.error("获取交易日历异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取历史数据
     * 
     * @param symbol    股票代码
     * @param market    市场（US/HK）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param kType     K线类型
     * @return K线数据列表
     */
    public List<Map<String, Object>> getHistoricalData(String symbol, String market, LocalDateTime startTime,
            LocalDateTime endTime,
            KType kType) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 先从数据库获取数据
            List<HistoricalData> dbData = historicalDataRepository
                    .findBySymbolAndkTypeAndDateTimeBetweenOrderByDateTimeAsc(symbol, kType.name(), startTime, endTime);

            // 如果数据库中有数据，检查数据完整性
            if (!dbData.isEmpty()) {
                // 获取预期的K线数量
                int expectedCount = calculateKlineCount(market, kType, startTime, endTime);

                // 如果数据库中的数据量小于预期，从API重新获取
                if (dbData.size() < expectedCount) {
                    logger.warn("数据库中的数据不完整 - 股票: {}, 市场: {}, K线类型: {}, 实际数量: {}, 预期数量: {}",
                            symbol, market, kType, dbData.size(), expectedCount);
                } else {
                    // 数据完整，直接返回数据库中的数据
                    logger.info("从数据库获取历史数据 - 股票: {}, 市场: {}, K线类型: {}, 数据点数: {}",
                            symbol, market, kType, dbData.size());
                    for (HistoricalData data : dbData) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("symbol", data.getSymbol());
                        map.put("time", Date.from(data.getDateTime().atZone(ZoneId.systemDefault()).toInstant()));
                        map.put("open", data.getOpen());
                        map.put("high", data.getHigh());
                        map.put("low", data.getLow());
                        map.put("close", data.getClose());
                        map.put("volume", data.getVolume());
                        result.add(map);
                    }
                    return result;
                }
            }

            // 如果数据库中没有数据或数据不完整，从API获取
            String pageToken = null;
            boolean reachEndTime = false;
            List<HistoricalData> dataToSave = new ArrayList<>();

            do {
                // 构建K线请求
                QuoteKlineRequest request = QuoteKlineRequest.newRequest(
                        Collections.singletonList(symbol),
                        kType,
                        DateUtils.printDate(startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                TimeZoneId.NewYork),
                        "-1");

                if (pageToken != null) {
                    request.withPageToken(pageToken);
                }

                // 执行请求
                QuoteKlineResponse response = tigerClient.execute(request);
                if (!response.isSuccess()) {
                    logger.error("获取K线数据失败 - 股票: {}, 市场: {}, 错误码: {}, 错误信息: {}",
                            symbol, market, response.getCode(), response.getMessage());
                    break;
                }

                // 处理响应数据
                List<KlineItem> items = response.getKlineItems();
                if (items == null || items.isEmpty()) {
                    logger.warn("未获取到K线数据 - 股票: {}, 市场: {}", symbol, market);
                    break;
                }

                // 处理每个K线数据
                for (KlineItem item : items) {
                    List<KlinePoint> points = item.getItems();
                    if (points == null || points.isEmpty()) {
                        continue;
                    }

                    for (KlinePoint point : points) {
                        LocalDateTime itemTime = LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(point.getTime()),
                                ZoneId.systemDefault());

                        // 如果数据时间超过结束时间，停止获取
                        if (itemTime.isAfter(endTime)) {
                            reachEndTime = true;
                            break;
                        }

                        // 如果数据时间在开始时间之前，跳过
                        if (itemTime.isBefore(startTime)) {
                            continue;
                        }

                        // 创建数据实体并保存
                        HistoricalData data = new HistoricalData();
                        data.setSymbol(symbol);
                        data.setDateTime(itemTime);
                        data.setOpen(point.getOpen());
                        data.setHigh(point.getHigh());
                        data.setLow(point.getLow());
                        data.setClose(point.getClose());
                        data.setVolume(point.getVolume());
                        data.setKType(kType.name());
                        dataToSave.add(data);

                        // 添加到结果集
                        Map<String, Object> kline = new HashMap<>();
                        kline.put("symbol", symbol);
                        kline.put("time", Date.from(itemTime.atZone(ZoneId.systemDefault()).toInstant()));
                        kline.put("open", point.getOpen());
                        kline.put("high", point.getHigh());
                        kline.put("low", point.getLow());
                        kline.put("close", point.getClose());
                        kline.put("volume", point.getVolume());
                        result.add(kline);
                    }

                    if (reachEndTime) {
                        break;
                    }

                    pageToken = item.getNextPageToken();
                }

            } while (pageToken != null && !reachEndTime);

            // 批量保存数据到数据库
            if (!dataToSave.isEmpty()) {
                // 如果是重新获取数据，先删除旧数据
                if (!dbData.isEmpty()) {
                    historicalDataRepository.deleteAll(dbData);
                    logger.info("删除旧数据 - 股票: {}, 市场: {}, K线类型: {}, 数据点数: {}",
                            symbol, market, kType, dbData.size());
                }

                historicalDataRepository.saveAll(dataToSave);
                logger.info("保存K线数据成功 - 股票: {}, 市场: {}, K线类型: {}, 数据点数: {}",
                        symbol, market, kType, dataToSave.size());
            }

            // 按时间排序
            result.sort((a, b) -> ((Date) a.get("time")).compareTo((Date) b.get("time")));

            logger.info("获取K线数据成功 - 股票: {}, 市场: {}, K线类型: {}, 数据点数: {}",
                    symbol, market, kType, result.size());
        } catch (Exception e) {
            logger.error("获取K线数据异常 - 股票: {}, 市场: {}", symbol, market, e);
        }

        return result;
    }

    /**
     * 计算指定时间范围内的K线数量
     * 
     * @param market    市场（US/HK）
     * @param kType     K线类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 预计的K线数量
     */
    public int calculateKlineCount(String market, KType kType, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 获取交易日历
            List<TradeCalendar> tradingDays = getTradingDays(market, startTime, endTime);
            if (tradingDays.isEmpty()) {
                return 0;
            }

            int tradingDaysCount = tradingDays.size();
            logger.info("交易日数量: {}", tradingDaysCount);

            // 根据不同的K线类型计算数量
            String kTypeName = kType.name().toLowerCase();
            int count;

            if (kTypeName.equals("day")) {
                count = tradingDaysCount;
            } else if (kTypeName.equals("week")) {
                count = (int) Math.ceil(tradingDaysCount / 5.0);
            } else if (kTypeName.equals("month")) {
                count = (int) Math.ceil(tradingDaysCount / 21.0); // 假设每月平均21个交易日
            } else if (kTypeName.equals("year")) {
                count = (int) Math.ceil(tradingDaysCount / 252.0); // 假设每年平均252个交易日
            } else if (kTypeName.startsWith("min")) {
                // 分钟级别K线
                int minutesPerDay;
                if (market.equalsIgnoreCase("US")) {
                    minutesPerDay = 390; // 美股每天6.5小时 = 390分钟
                } else {
                    minutesPerDay = 330; // 港股每天5.5小时 = 330分钟
                }

                // 获取K线的分钟数
                int minutes;
                try {
                    minutes = Integer.parseInt(kTypeName.substring(3));
                } catch (NumberFormatException e) {
                    logger.error("无法解析K线周期: {}", kTypeName);
                    return 0;
                }

                // 验证分钟数是否有效
                if (minutes <= 0) {
                    logger.error("无效的分钟数: {}", minutes);
                    return 0;
                }

                // 计算每天的K线数量
                int barsPerDay = minutesPerDay / minutes;
                count = barsPerDay * tradingDaysCount;
            } else {
                logger.warn("不支持的K线类型: {}", kTypeName);
                return 0;
            }

            logger.info("计算K线数量 - 市场: {}, K线类型: {}, 开始时间: {}, 结束时间: {}, 数量: {}",
                    market, kType, startTime, endTime, count);
            return count;
        } catch (Exception e) {
            logger.error("计算K线数量失败", e);
            return 0;
        }
    }

}