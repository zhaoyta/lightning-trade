package com.lightningtrade.easyquant.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trade_records")
public class TradeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 订单ID
    @Column(name = "order_id")
    private Long orderId;

    // 股票代码
    private String symbol;

    // 交易数量
    private int quantity;

    // 交易价格
    private double price;

    // 交易方向（买入/卖出）
    private String action;

    // 订单类型（市价单/限价单）
    @Column(name = "order_type")
    private String orderType;

    // 交易状态
    private String status;

    // 成交金额
    private double amount;

    // 交易费用
    private double fee;

    // 交易时间
    @Column(name = "trade_time")
    private LocalDateTime tradeTime;

    // 创建时间
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}