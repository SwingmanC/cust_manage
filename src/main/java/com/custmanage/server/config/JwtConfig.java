package com.custmanage.server.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /** 可通过环境变量 JWT_SECRET 固定密钥，不配置则每次重启随机生成 */
    private String secret;
    private long expiration = 86400000L;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            byte[] key = new byte[32];
            new SecureRandom().nextBytes(key);
            this.secret = Base64.getEncoder().encodeToString(key);
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
