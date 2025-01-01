package com.lightningtrade.easyquant.model;

import com.tigerbrokers.stock.openapi.client.struct.enums.ActionType;
import com.tigerbrokers.stock.openapi.client.struct.enums.Currency;
import com.tigerbrokers.stock.openapi.client.struct.enums.OrderType;
import com.tigerbrokers.stock.openapi.client.struct.enums.SecType;
import lombok.Data;
import lombok.Builder;

/**
 * 交易订单
 */
@Data
@Builder
public class TradeOrder {
    // 订单ID
    private Long orderId;

    // 股票代码
    private String symbol;

    // 交易数量
    private int quantity;

    // 交易价格（限价单需要）
    private Double price;

    // 订单类型（市价单/限价单）
    private OrderType orderType;

    // 证券类型（股票/期权等）
    private SecType secType;

    // 货币类型
    private Currency currency;

    // 交易方向（买入/卖出）
    private ActionType action;

    // 订单状态
    private String status;

    // 成交均价
    private Double avgFillPrice;

    // 已成交数量
    private int filledQuantity;

    // 剩余数量
    private int remainingQuantity;

    // 委托时间
    private Long createTime;

    // 更新时间
    private Long updateTime;
}