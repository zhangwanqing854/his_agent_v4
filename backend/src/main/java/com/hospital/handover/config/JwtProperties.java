package com.hospital.handover.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    private String secret = "handover-system-secret-key-very-long-for-security-2024";
    private long expiration = 86400000;
    private String header = "Authorization";
    private String prefix = "Bearer ";
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
    
    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
    
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
}