package com.lightningtrade.easyquant.config;

import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.struct.enums.Env;
import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class ApiConfig {

    @Autowired
    private TigerApiConfig tigerApiConfig;

    @Bean
    public TigerHttpClient tigerHttpClient() {
        try {
            // 配置客户端
            ClientConfig clientConfig = ClientConfig.DEFAULT_CONFIG;

            // 设置Tiger API配置
            clientConfig.tigerId = tigerApiConfig.getTigerId();
            clientConfig.privateKey = tigerApiConfig.getPrivateKeyPk8();
            clientConfig.defaultAccount = tigerApiConfig.getAccount();
            clientConfig.setEnv(Env.getEnv(tigerApiConfig.getEnv()));

            // 使用配置初始化客户端
            return TigerHttpClient.getInstance().clientConfig(clientConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize API client: " + e.getMessage(), e);
        }
    }
}
