package com.hospital.handover.service;

import com.hospital.handover.dto.SmsConfigDto;
import com.hospital.handover.dto.SmsConfigResponse;
import com.hospital.handover.entity.SmsConfig;
import com.hospital.handover.repository.SmsConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmsConfigServiceTest {

    @Mock
    private SmsConfigRepository smsConfigRepository;

    private SmsConfigService smsConfigService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        smsConfigService = new SmsConfigService(smsConfigRepository);
    }

    @Test
    void testGetAllConfigsSensitiveFieldsMasked() {
        SmsConfig enabledConfig = new SmsConfig();
        enabledConfig.setConfigKey("enabled");
        enabledConfig.setConfigValue("true");
        
        SmsConfig providerConfig = new SmsConfig();
        providerConfig.setConfigKey("provider");
        providerConfig.setConfigValue("aliyun");
        
        SmsConfig accessKeyConfig = new SmsConfig();
        accessKeyConfig.setConfigKey("aliyun_access_key_id");
        accessKeyConfig.setConfigValue("LTAI1234567890");
        accessKeyConfig.setIsSensitive(true);
        accessKeyConfig.setDescription("阿里云 AccessKey ID");
        
        when(smsConfigRepository.findByConfigKey("enabled")).thenReturn(enabledConfig);
        when(smsConfigRepository.findByConfigKey("provider")).thenReturn(providerConfig);
        when(smsConfigRepository.findAll()).thenReturn(Arrays.asList(enabledConfig, providerConfig, accessKeyConfig));
        
        SmsConfigResponse response = smsConfigService.getAllConfigs();
        
        assertTrue(response.getEnabled());
        assertEquals("aliyun", response.getProvider());
        
        List<SmsConfigDto> configs = response.getConfigs();
        assertFalse(configs.isEmpty());
        
        SmsConfigDto sensitiveDto = configs.stream()
            .filter(c -> c.getKey().equals("aliyun_access_key_id"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(sensitiveDto);
        assertNull(sensitiveDto.getValue());
        assertEquals("LTAI****90", sensitiveDto.getMaskedValue());
    }

    @Test
    void testMaskShortValue() {
        SmsConfig accessKeyConfig = new SmsConfig();
        accessKeyConfig.setConfigKey("aliyun_access_key_id");
        accessKeyConfig.setConfigValue("abcd");
        accessKeyConfig.setIsSensitive(true);
        accessKeyConfig.setDescription("测试");
        
        when(smsConfigRepository.findByConfigKey("enabled")).thenReturn(null);
        when(smsConfigRepository.findByConfigKey("provider")).thenReturn(null);
        when(smsConfigRepository.findAll()).thenReturn(Arrays.asList(accessKeyConfig));
        
        SmsConfigResponse response = smsConfigService.getAllConfigs();
        SmsConfigDto dto = response.getConfigs().get(0);
        
        assertEquals("abcd****", dto.getMaskedValue());
    }

    @Test
    void testMaskEmptyValue() {
        SmsConfig accessKeyConfig = new SmsConfig();
        accessKeyConfig.setConfigKey("aliyun_access_key_id");
        accessKeyConfig.setConfigValue("");
        accessKeyConfig.setIsSensitive(true);
        accessKeyConfig.setDescription("测试");
        
        when(smsConfigRepository.findByConfigKey("enabled")).thenReturn(null);
        when(smsConfigRepository.findByConfigKey("provider")).thenReturn(null);
        when(smsConfigRepository.findAll()).thenReturn(Arrays.asList(accessKeyConfig));
        
        SmsConfigResponse response = smsConfigService.getAllConfigs();
        SmsConfigDto dto = response.getConfigs().get(0);
        
        assertEquals("", dto.getMaskedValue());
    }

    @Test
    void testIsEnabled() {
        SmsConfig config = new SmsConfig();
        config.setConfigKey("enabled");
        config.setConfigValue("true");
        
        when(smsConfigRepository.findByConfigKey("enabled")).thenReturn(config);
        
        assertTrue(smsConfigService.isEnabled());
        
        config.setConfigValue("false");
        assertFalse(smsConfigService.isEnabled());
    }

    @Test
    void testGetConfigValue() {
        SmsConfig config = new SmsConfig();
        config.setConfigKey("test_key");
        config.setConfigValue("test_value");
        
        when(smsConfigRepository.findByConfigKey("test_key")).thenReturn(config);
        
        assertEquals("test_value", smsConfigService.getConfigValue("test_key"));
    }
}