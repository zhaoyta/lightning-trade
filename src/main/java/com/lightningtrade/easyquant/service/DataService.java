package com.lightningtrade.easyquant.service;

import com.lightningtrade.easyquant.entity.HistoricalData;
import com.lightningtrade.easyquant.repository.HistoricalDataRepository;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.HistoryTimelineItem;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.TimelinePoint;
import com.tigerbrokers.stock.openapi.client.https.request.quote.QuoteHistoryTimelineRequest;
import com.tigerbrokers.stock.openapi.client.https.response.quote.QuoteHistoryTimelineResponse;
import com.tigerbrokers.stock.openapi.client.struct.enums.Language;
import com.tigerbrokers.stock.openapi.client.struct.enums.TimeZoneId;
import com.tigerbrokers.stock.openapi.client.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DataService {
    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Autowired
    private TigerHttpClient client;

    @Autowired
    private HistoricalDataRepository historicalDataRepository;

    /**
     * 获取历史数据
     */
    public List<HistoricalData> getHistoricalData(String symbol, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("开始获取历史分时数据 - 股票: {}, 开始时间: {}, 结束时间: {}", symbol, startTime, endTime);

        // 先从数据库查询
        List<HistoricalData> histData = historicalDataRepository.findBySymbolAndDateTimeBetweenOrderByDateTimeAsc(
                symbol, startTime, endTime);

        if (!histData.isEmpty()) {
            logger.info("从数据库获取历史数据成功 - 数据点数量: {}", histData.size());
            return histData;
        }

        // 数据库没有，从API获取
        try {
            QuoteHistoryTimelineRequest request = QuoteHistoryTimelineRequest.newRequest(
                    Collections.singletonList(symbol),
                    DateUtils.printDate(startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            TimeZoneId.NewYork),
                    Language.en_US);

            QuoteHistoryTimelineResponse response = client.execute(request);

            if (!response.isSuccess()) {
                logger.error("获取历史分时数据失败 - 错误码: {}, 错误信息: {}",
                        response.getCode(), response.getMessage());
                return new ArrayList<>();
            }

            List<HistoryTimelineItem> items = response.getTimelineItems();
            if (items == null || items.isEmpty()) {
                logger.warn("未获取到历史分时数据");
                return new ArrayList<>();
            }

            // 转换并保存数据
            List<HistoricalData> dataList = new ArrayList<>();
            for (HistoryTimelineItem item : items) {
                List<TimelinePoint> points = item.getItems();
                if (points == null || points.isEmpty()) {
                    logger.warn("时间线项目中没有数据点 - 股票: {}", symbol);
                    continue;
                }

                for (TimelinePoint point : points) {
                    // 检查时间范围
                    LocalDateTime pointTime = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(point.getTime()),
                            ZoneId.systemDefault());

                    if (pointTime.isBefore(startTime) || pointTime.isAfter(endTime)) {
                        continue;
                    }

                    HistoricalData data = new HistoricalData();
                    data.setSymbol(symbol);
                    data.setDateTime(pointTime);
                    data.setOpen(point.getPrice());
                    data.setHigh(point.getPrice());
                    data.setLow(point.getPrice());
                    data.setClose(point.getPrice());
                    data.setVolume(point.getVolume());
                    data.setCreatedAt(LocalDateTime.now());
                    dataList.add(data);
                }
            }

            // 保存到数据库
            if (!dataList.isEmpty()) {
                historicalDataRepository.saveAll(dataList);
                logger.info("历史分时数据获取成功 - 数据点数量: {}", dataList.size());
            } else {
                logger.warn("未获取到任何有效的历史分时数据 - 股票: {}", symbol);
            }

            return dataList;
        } catch (Exception e) {
            logger.error("获取历史分时数据异常", e);
            return new ArrayList<>();
        }
    }
}