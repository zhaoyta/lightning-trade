package com.lightningtrade.easyquant.controller;

import com.lightningtrade.easyquant.config.TradingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private TradingConfig tradingConfig;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("usMarket", tradingConfig.getMarkets().get("us"));
        model.addAttribute("hkMarket", tradingConfig.getMarkets().get("hk"));
        return "index";
    }

    @GetMapping("/trading")
    public String trading(Model model) {
        model.addAttribute("usMarket", tradingConfig.getMarkets().get("us"));
        model.addAttribute("hkMarket", tradingConfig.getMarkets().get("hk"));
        return "trading";
    }

    @GetMapping("/backtest")
    public String backtest(Model model) {
        model.addAttribute("strategies", List.of("MA", "DOUBLE_MA", "MACD", "RSI", "BOLL"));
        return "backtest";
    }
}