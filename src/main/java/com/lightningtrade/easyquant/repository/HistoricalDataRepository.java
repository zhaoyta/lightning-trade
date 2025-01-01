package com.lightningtrade.easyquant.repository;

import com.lightningtrade.easyquant.entity.HistoricalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricalDataRepository extends JpaRepository<HistoricalData, Long> {

    /**
     * 根据股票代码和时间范围查询历史数据
     */
    List<HistoricalData> findBySymbolAndDateTimeBetweenOrderByDateTimeAsc(
            String symbol, LocalDateTime startTime, LocalDateTime endTime);
}