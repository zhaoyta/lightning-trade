package com.lightningtrade.easyquant.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historical_data")
public class HistoricalData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private LocalDateTime dateTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private String kType; // day, week, month, year, min1, min3, min5, min15, min30, min60, min120, min240

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}