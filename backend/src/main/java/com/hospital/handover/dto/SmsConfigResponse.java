package com.hospital.handover.dto;

import java.util.List;

public class SmsConfigResponse {
    
    private Boolean enabled;
    private String provider;
    private List<SmsConfigDto> configs;
    
    public SmsConfigResponse() {}
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public List<SmsConfigDto> getConfigs() { return configs; }
    public void setConfigs(List<SmsConfigDto> configs) { this.configs = configs; }
}