package com.hospital.handover.service;

import com.hospital.handover.dto.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultSmsService implements SmsService {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultSmsService.class);
    
    private final SmsConfigService smsConfigService;
    
    public DefaultSmsService(SmsConfigService smsConfigService) {
        this.smsConfigService = smsConfigService;
    }
    
    @Override
    public SmsResult send(String phone, String content) {
        String provider = smsConfigService.getConfigValue("provider");
        
        if ("aliyun".equals(provider)) {
            return sendViaAliyun(phone, content);
        }
        
        log.info("[DefaultSmsService] 发送短信到 {} : {}", phone, content);
        return SmsResult.success();
    }
    
    private SmsResult sendViaAliyun(String phone, String content) {
        String accessKeyId = smsConfigService.getConfigValue("aliyun_access_key_id");
        String accessKeySecret = smsConfigService.getConfigValue("aliyun_access_key_secret");
        String signName = smsConfigService.getConfigValue("aliyun_sign_name");
        String templateCode = smsConfigService.getConfigValue("aliyun_template_code");
        
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            log.warn("阿里云 AccessKey ID 未配置");
            return SmsResult.failure("阿里云 AccessKey ID 未配置");
        }
        
        if (accessKeySecret == null || accessKeySecret.isEmpty()) {
            log.warn("阿里云 AccessKey Secret 未配置");
            return SmsResult.failure("阿里云 AccessKey Secret 未配置");
        }
        
        log.info("[Aliyun] 发送短信到 {} : {} (signName={}, templateCode={})", 
                 phone, content, signName, templateCode);
        
        return SmsResult.success();
    }
}