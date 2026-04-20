package com.hospital.handover.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "his.soap")
public class HisSoapProperties {
    
    private String url;
    private String namespace;
    private String accessToken;
    private int timeoutMs = 30000;
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    
    public String getFullUrl(String servicePath) {
        String baseUrl = url;
        if (accessToken != null && !accessToken.isEmpty()) {
            baseUrl += "?access_token=" + accessToken;
        }
        if (servicePath != null && !servicePath.isEmpty()) {
            baseUrl += servicePath.startsWith("/") ? servicePath : "/" + servicePath;
        }
        return baseUrl;
    }
}