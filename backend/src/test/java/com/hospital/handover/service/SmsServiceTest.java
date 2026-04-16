package com.hospital.handover.service;

import com.hospital.handover.dto.SmsResult;
import com.hospital.handover.entity.SmsConfig;
import com.hospital.handover.repository.SmsConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmsServiceTest {

    @Mock
    private SmsConfigRepository smsConfigRepository;

    private DefaultSmsService smsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SmsConfigService smsConfigService = new SmsConfigService(smsConfigRepository);
        smsService = new DefaultSmsService(smsConfigService);
    }

    @Test
    void testSendWithoutConfig() {
        when(smsConfigRepository.findByConfigKey("provider")).thenReturn(null);
        when(smsConfigRepository.findByConfigKey("aliyun_access_key_id")).thenReturn(null);
        
        SmsResult result = smsService.send("13800138000", "Test message");
        
        assertTrue(result.isSuccess());
    }

    @Test
    void testSendAliyunWithoutAccessKey() {
        SmsConfig providerConfig = new SmsConfig();
        providerConfig.setConfigKey("provider");
        providerConfig.setConfigValue("aliyun");
        
        when(smsConfigRepository.findByConfigKey("provider")).thenReturn(providerConfig);
        when(smsConfigRepository.findByConfigKey("aliyun_access_key_id")).thenReturn(null);
        
        SmsResult result = smsService.send("13800138000", "Test message");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("AccessKey"));
    }
}