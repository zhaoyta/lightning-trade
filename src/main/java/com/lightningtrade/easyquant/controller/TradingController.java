package com.lightningtrade.easyquant.controller;

import com.lightningtrade.easyquant.service.HKTradingService;
import com.lightningtrade.easyquant.service.USTradingService;
import com.lightningtrade.easyquant.config.TradingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易控制器
 * 提供实时交易系统的REST API接口，用于管理和监控不同市场的交易状态和配置
 * 主要功能：
 * 1. 获取交易系统状态（各市场启用状态、交易标的、策略信息）
 * 2. 查询交易系统配置
 * 3. 获取特定市场的交易标的和策略信息
 * 
 * API端点：
 * - GET /api/trading/status：获取所有市场的交易状态
 * - GET /api/trading/config：获取交易系统配置
 * - GET /api/trading/markets/{market}/symbols：获取指定市场的交易标的
 * - GET /api/trading/markets/{market}/strategy：获取指定市场的交易策略
 */
@RestController
@RequestMapping("/api/trading")
public class TradingController {

    // 港股交易服务
    @Autowired
    private HKTradingService hkTradingService;

    // 美股交易服务
    @Autowired
    private USTradingService usTradingService;

    // 交易系统配置
    @Autowired
    private TradingConfig tradingConfig;

    /**
     * 获取交易系统状态
     * 返回各个市场（港股、美股）的运行状态、交易标的和策略信息
     * 
     * @return 包含各市场状态信息的Map：
     *         - enabled: 市场是否启用
     *         - symbols: 交易标的列表
     *         - strategy: 当前使用的交易策略（仅在市场启用时返回）
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();

        // 获取港股市场状态
        Map<String, Object> hkStatus = new HashMap<>();
        hkStatus.put("enabled", hkTradingService.isEnabled());
        hkStatus.put("symbols", hkTradingService.getSymbols());
        if (hkTradingService.isEnabled()) {
            hkStatus.put("strategy", tradingConfig.getMarkets().get("hk").getStrategy());
        }
        status.put("hk", hkStatus);

        // 获取美股市场状态
        Map<String, Object> usStatus = new HashMap<>();
        usStatus.put("enabled", usTradingService.isEnabled());
        usStatus.put("symbols", usTradingService.getSymbols());
        if (usTradingService.isEnabled()) {
            usStatus.put("strategy", tradingConfig.getMarkets().get("us").getStrategy());
        }
        status.put("us", usStatus);

        return ResponseEntity.ok(status);
    }

    /**
     * 获取交易系统配置
     * 返回完整的交易系统配置信息，包括各市场的配置参数
     * 
     * @return 交易系统配置对象
     */
    @GetMapping("/config")
    public ResponseEntity<TradingConfig> getConfig() {
        return ResponseEntity.ok(tradingConfig);
    }

    /**
     * 获取指定市场的交易标的列表
     * 
     * @param market 市场代码（US-美股，HK-港股）
     * @return 交易标的列表，如果市场未启用或不存在则返回404
     */
    @GetMapping("/markets/{market}/symbols")
    public ResponseEntity<List<TradingConfig.Symbol>> getMarketSymbols(@PathVariable String market) {
        if (market.equals("US") && usTradingService.isEnabled()) {
            return ResponseEntity.ok(usTradingService.getSymbols());
        } else if (market.equals("HK") && hkTradingService.isEnabled()) {
            return ResponseEntity.ok(hkTradingService.getSymbols());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 获取指定市场的交易策略配置
     * 
     * @param market 市场代码（US-美股，HK-港股）
     * @return 市场的策略配置，如果市场不存在则返回404
     */
    @GetMapping("/markets/{market}/strategy")
    public ResponseEntity<TradingConfig.Strategy> getMarketStrategy(@PathVariable String market) {
        if (tradingConfig.getMarkets() == null || !tradingConfig.getMarkets().containsKey(market.toLowerCase())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tradingConfig.getMarkets().get(market.toLowerCase()).getStrategy());
    }
}
