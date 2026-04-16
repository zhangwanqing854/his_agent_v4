package com.hospital.handover.service;

import com.hospital.handover.dto.SmsConfigDto;
import com.hospital.handover.dto.SmsConfigResponse;
import com.hospital.handover.dto.SmsConfigUpdateRequest;
import com.hospital.handover.entity.SmsConfig;
import com.hospital.handover.repository.SmsConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SmsConfigService {
    
    private final SmsConfigRepository smsConfigRepository;
    
    public SmsConfigService(SmsConfigRepository smsConfigRepository) {
        this.smsConfigRepository = smsConfigRepository;
    }
    
    public boolean isEnabled() {
        SmsConfig config = smsConfigRepository.findByConfigKey("enabled");
        return config != null && "true".equals(config.getConfigValue());
    }
    
    public String getConfigValue(String key) {
        SmsConfig config = smsConfigRepository.findByConfigKey(key);
        return config != null ? config.getConfigValue() : null;
    }
    
    public SmsConfigResponse getAllConfigs() {
        SmsConfigResponse response = new SmsConfigResponse();
        
        SmsConfig enabledConfig = smsConfigRepository.findByConfigKey("enabled");
        response.setEnabled(enabledConfig != null && "true".equals(enabledConfig.getConfigValue()));
        
        SmsConfig providerConfig = smsConfigRepository.findByConfigKey("provider");
        response.setProvider(providerConfig != null ? providerConfig.getConfigValue() : "aliyun");
        
        List<SmsConfigDto> configs = new ArrayList<>();
        List<SmsConfig> allConfigs = smsConfigRepository.findAll();
        
        for (SmsConfig config : allConfigs) {
            if ("enabled".equals(config.getConfigKey()) || "provider".equals(config.getConfigKey())) {
                continue;
            }
            
            SmsConfigDto dto = new SmsConfigDto();
            dto.setKey(config.getConfigKey());
            dto.setDescription(config.getDescription());
            dto.setIsSensitive(config.getIsSensitive());
            
            if (config.getIsSensitive() != null && config.getIsSensitive()) {
                dto.setValue(null);
                dto.setMaskedValue(maskValue(config.getConfigValue()));
            } else {
                dto.setValue(config.getConfigValue());
                dto.setMaskedValue(config.getConfigValue());
            }
            
            configs.add(dto);
        }
        
        response.setConfigs(configs);
        return response;
    }
    
    @Transactional
    public void updateConfigs(SmsConfigUpdateRequest request) {
        updateConfig("enabled", request.getEnabled() != null ? request.getEnabled().toString() : "false");
        updateConfig("provider", request.getProvider() != null ? request.getProvider() : "aliyun");
        
        if (request.getAliyunAccessKeyId() != null && !request.getAliyunAccessKeyId().isEmpty()) {
            updateConfig("aliyun_access_key_id", request.getAliyunAccessKeyId());
        }
        if (request.getAliyunAccessKeySecret() != null && !request.getAliyunAccessKeySecret().isEmpty()) {
            updateConfig("aliyun_access_key_secret", request.getAliyunAccessKeySecret());
        }
        if (request.getAliyunSignName() != null) {
            updateConfig("aliyun_sign_name", request.getAliyunSignName());
        }
        if (request.getAliyunTemplateCode() != null) {
            updateConfig("aliyun_template_code", request.getAliyunTemplateCode());
        }
    }
    
    private void updateConfig(String key, String value) {
        SmsConfig config = smsConfigRepository.findByConfigKey(key);
        if (config != null) {
            config.setConfigValue(value);
            smsConfigRepository.save(config);
        }
    }
    
    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= 8) {
            return value.substring(0, Math.min(4, value.length())) + "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 2);
    }
}