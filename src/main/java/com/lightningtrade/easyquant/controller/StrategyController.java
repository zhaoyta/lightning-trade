package com.lightningtrade.easyquant.controller;

import com.lightningtrade.easyquant.strategy.MACrossStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {
    
    @GetMapping("/test")
    public String test() {
        return "Strategy system is running!";
    }
} 