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

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    @Autowired
    private HKTradingService hkTradingService;

    @Autowired
    private USTradingService usTradingService;

    @Autowired
    private TradingConfig tradingConfig;

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

    @GetMapping("/config")
    public ResponseEntity<TradingConfig> getConfig() {
        return ResponseEntity.ok(tradingConfig);
    }

    @GetMapping("/markets/{market}/symbols")
    public ResponseEntity<List<TradingConfig.Symbol>> getMarketSymbols(@PathVariable String market) {
        if (market.equals("US") && usTradingService.isEnabled()) {
            return ResponseEntity.ok(usTradingService.getSymbols());
        } else if (market.equals("HK") && hkTradingService.isEnabled()) {
            return ResponseEntity.ok(hkTradingService.getSymbols());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/markets/{market}/strategy")
    public ResponseEntity<TradingConfig.Strategy> getMarketStrategy(@PathVariable String market) {
        if (tradingConfig.getMarkets() == null || !tradingConfig.getMarkets().containsKey(market.toLowerCase())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tradingConfig.getMarkets().get(market.toLowerCase()).getStrategy());
    }
}
