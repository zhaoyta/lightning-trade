package com.lightningtrade.easyquant.repository;

import com.lightningtrade.easyquant.entity.HistoricalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricalDataRepository extends JpaRepository<HistoricalData, Long> {

    /**
     * 根据股票代码和时间范围查询历史数据
     */
    @Query("SELECT h FROM HistoricalData h WHERE h.symbol = :symbol AND h.kType = :kType AND h.dateTime BETWEEN :startTime AND :endTime ORDER BY h.dateTime ASC")
    List<HistoricalData> findBySymbolAndkTypeAndDateTimeBetweenOrderByDateTimeAsc(
            @Param("symbol") String symbol,
            @Param("kType") String kType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}