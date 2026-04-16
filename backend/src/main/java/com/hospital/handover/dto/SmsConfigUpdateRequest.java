package com.hospital.handover.dto;

public class SmsConfigUpdateRequest {
    
    private Boolean enabled;
    private String provider;
    private String aliyunAccessKeyId;
    private String aliyunAccessKeySecret;
    private String aliyunSignName;
    private String aliyunTemplateCode;
    
    public SmsConfigUpdateRequest() {}
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getAliyunAccessKeyId() { return aliyunAccessKeyId; }
    public void setAliyunAccessKeyId(String aliyunAccessKeyId) { this.aliyunAccessKeyId = aliyunAccessKeyId; }
    
    public String getAliyunAccessKeySecret() { return aliyunAccessKeySecret; }
    public void setAliyunAccessKeySecret(String aliyunAccessKeySecret) { this.aliyunAccessKeySecret = aliyunAccessKeySecret; }
    
    public String getAliyunSignName() { return aliyunSignName; }
    public void setAliyunSignName(String aliyunSignName) { this.aliyunSignName = aliyunSignName; }
    
    public String getAliyunTemplateCode() { return aliyunTemplateCode; }
    public void setAliyunTemplateCode(String aliyunTemplateCode) { this.aliyunTemplateCode = aliyunTemplateCode; }
}