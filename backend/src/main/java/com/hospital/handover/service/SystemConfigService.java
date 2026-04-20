package com.hospital.handover.service;

import com.hospital.handover.entity.SystemConfig;
import com.hospital.handover.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SystemConfigService {
    
    private final SystemConfigRepository systemConfigRepository;
    
    public SystemConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }
    
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }
    
    public String getConfig(String key) {
        return systemConfigRepository.findByConfigKey(key)
            .map(SystemConfig::getConfigValue)
            .orElse(null);
    }
    
    public void updateConfig(String key, String value) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
            .orElseThrow(() -> new RuntimeException("配置项不存在: " + key));
        config.setConfigValue(value);
        systemConfigRepository.save(config);
    }
    
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            updateConfig(entry.getKey(), entry.getValue());
        }
    }
}