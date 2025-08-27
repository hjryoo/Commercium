package com.commercium.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiry = 3600; // 1시간
    private long refreshTokenExpiry = 2592000; // 30일
}

