package com.lightningtrade.easyquant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 量化交易系统主应用类
 * 基于Spring Boot框架，集成老虎证券API，实现量化交易功能
 */
@SpringBootApplication
@EnableScheduling
public class LightningTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(LightningTradeApplication.class, args);
    }
}