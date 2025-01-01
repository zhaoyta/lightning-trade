package com.lightningtrade.easyquant.model;

import lombok.Data;

/**
 * 持仓信息
 */
@Data
public class Position {
    // 股票代码
    private String symbol;

    // 持仓数量
    private int quantity;

    // 开仓价格
    private double entryPrice;

    // 当前市值
    private double marketValue;

    // 未实现盈亏
    private double unrealizedPnL;

    // 已实现盈亏
    private double realizedPnL;

    public Position() {
        this.quantity = 0;
        this.entryPrice = 0.0;
        this.marketValue = 0.0;
        this.unrealizedPnL = 0.0;
        this.realizedPnL = 0.0;
    }

    public Position(String symbol) {
        this();
        this.symbol = symbol;
    }

    /**
     * 是否持仓
     */
    public boolean isHolding() {
        return this.quantity > 0;
    }

    /**
     * 买入
     */
    public void buy(double price, int quantity) {
        if (this.quantity > 0) {
            // 如果已有持仓，计算新的平均成本
            this.entryPrice = (this.entryPrice * this.quantity + price * quantity) / (this.quantity + quantity);
        } else {
            // 新建仓位
            this.entryPrice = price;
        }
        this.quantity += quantity;
        updateMarketValue(price);
    }

    /**
     * 卖出
     */
    public void sell(double price) {
        if (this.quantity > 0) {
            // 计算已实现盈亏
            this.realizedPnL += (price - this.entryPrice) * this.quantity;
            // 清空持仓
            this.quantity = 0;
            this.entryPrice = 0;
            this.marketValue = 0;
            this.unrealizedPnL = 0;
        }
    }

    /**
     * 更新持仓市值和未实现盈亏
     */
    public void updateMarketValue(double currentPrice) {
        this.marketValue = this.quantity * currentPrice;
        if (this.quantity > 0) {
            this.unrealizedPnL = this.marketValue - this.quantity * this.entryPrice;
        } else {
            this.unrealizedPnL = 0.0;
        }
    }

    /**
     * 获取总盈亏（已实现 + 未实现）
     */
    public double getPnL() {
        return this.realizedPnL + this.unrealizedPnL;
    }
}