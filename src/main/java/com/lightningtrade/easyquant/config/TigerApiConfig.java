package com.lightningtrade.easyquant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import lombok.Data;

@Configuration
@PropertySource(value = "file:${tiger.config.path}", ignoreResourceNotFound = true)
@Data
public class TigerApiConfig {

    @Value("${private_key_pk1}")
    private String privateKeyPk1;

    @Value("${private_key_pk8}")
    private String privateKeyPk8;

    @Value("${tiger_id}")
    private String tigerId;

    @Value("${account}")
    private String account;

    @Value("${license}")
    private String license;

    @Value("${env}")
    private String env;

}