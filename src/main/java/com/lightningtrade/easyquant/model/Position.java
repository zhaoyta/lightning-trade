package com.lightningtrade.easyquant.model;

import lombok.Data;

/**
 * 持仓位置模型类
 * 用于跟踪和管理单个证券的持仓情况，包括数量、成本、市值和盈亏等信息
 * 主要功能：
 * 1. 记录持仓基本信息（代码、数量、均价等）
 * 2. 处理买入卖出操作
 * 3. 计算和更新持仓市值
 * 4. 跟踪已实现和未实现盈亏
 */
@Data
public class Position {
    // 证券代码
    private String symbol;
    // 持仓数量
    private int quantity;
    // 持仓平均成本
    private double averagePrice;
    // 当前市场价格
    private double currentPrice;
    // 持仓市值（数量 * 当前价格）
    private double marketValue;
    // 未实现盈亏（当前市值 - 持仓成本）
    private double unrealizedPnL;
    // 已实现盈亏（历史卖出的盈亏总和）
    private double realizedPnL;

    /**
     * 默认构造函数
     * 初始化所有数值为0
     */
    public Position() {
        this.quantity = 0;
        this.averagePrice = 0.0;
        this.currentPrice = 0.0;
        this.marketValue = 0.0;
        this.unrealizedPnL = 0.0;
        this.realizedPnL = 0.0;
    }

    /**
     * 带证券代码的构造函数
     * 
     * @param symbol 证券代码
     */
    public Position(String symbol) {
        this();
        this.symbol = symbol;
    }

    /**
     * 检查是否有持仓
     * 
     * @return true表示有持仓（数量大于0），false表示无持仓
     */
    public boolean isHolding() {
        return this.quantity > 0;
    }

    /**
     * 处理买入操作
     * 更新持仓数量和平均成本
     * 如果已有持仓，则计算新的加权平均成本
     * 如果是新建仓位，则直接使用买入价格作为成本
     * 
     * @param price    买入价格
     * @param quantity 买入数量
     */
    public void buy(double price, int quantity) {
        if (this.quantity > 0) {
            // 如果已有持仓，计算新的平均成本
            this.averagePrice = (this.averagePrice * this.quantity + price * quantity) / (this.quantity + quantity);
        } else {
            // 新建仓位
            this.averagePrice = price;
        }
        this.quantity += quantity;
        updateMarketValue(price);
    }

    /**
     * 处理卖出操作
     * 计算已实现盈亏并清空持仓信息
     * 已实现盈亏 = (卖出价格 - 平均成本) * 卖出数量
     * 
     * @param price 卖出价格
     */
    public void sell(double price) {
        if (this.quantity > 0) {
            // 计算已实现盈亏
            this.realizedPnL += (price - this.averagePrice) * this.quantity;
            // 清空持仓
            this.quantity = 0;
            this.averagePrice = 0;
            this.marketValue = 0;
            this.unrealizedPnL = 0;
        }
    }

    /**
     * 更新持仓市值和未实现盈亏
     * 市值 = 当前价格 * 持仓数量
     * 未实现盈亏 = 市值 - 持仓成本
     * 
     * @param currentPrice 当前市场价格
     */
    public void updateMarketValue(double currentPrice) {
        this.currentPrice = currentPrice;
        this.marketValue = this.quantity * currentPrice;
        if (this.quantity > 0) {
            this.unrealizedPnL = this.marketValue - this.quantity * this.averagePrice;
        } else {
            this.unrealizedPnL = 0.0;
        }
    }

    /**
     * 获取总盈亏
     * 总盈亏 = 已实现盈亏 + 未实现盈亏
     * 
     * @return 总盈亏金额
     */
    public double getPnL() {
        return this.realizedPnL + this.unrealizedPnL;
    }
}