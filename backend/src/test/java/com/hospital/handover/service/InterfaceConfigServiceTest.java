package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.InterfaceConfig;
import com.hospital.handover.repository.InterfaceConfigRepository;
import com.hospital.handover.repository.InterfaceFieldMappingRepository;
import com.hospital.handover.repository.InterfaceMappingTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterfaceConfigServiceTest {

    private InterfaceConfigRepository configRepository;
    private InterfaceMappingTableRepository mappingTableRepository;
    private InterfaceFieldMappingRepository fieldMappingRepository;
    private InterfaceConfigService configService;

    @BeforeEach
    void setUp() {
        configRepository = mock(InterfaceConfigRepository.class);
        mappingTableRepository = mock(InterfaceMappingTableRepository.class);
        fieldMappingRepository = mock(InterfaceFieldMappingRepository.class);
        configService = new InterfaceConfigService(configRepository, mappingTableRepository, fieldMappingRepository);
    }

    @Test
    void testGetAllConfigs() {
        InterfaceConfig config1 = new InterfaceConfig();
        config1.setId(1L);
        config1.setConfigCode("HIS_DEPT_SYNC");
        config1.setConfigName("科室信息同步");
        
        InterfaceConfig config2 = new InterfaceConfig();
        config2.setId(2L);
        config2.setConfigCode("HIS_STAFF_SYNC");
        config2.setConfigName("人员信息同步");
        
        when(configRepository.findAll()).thenReturn(Arrays.asList(config1, config2));
        
        var result = configService.getAllConfigs();
        
        assertEquals(2, result.size());
        assertEquals("HIS_DEPT_SYNC", result.get(0).getConfigCode());
    }

    @Test
    void testGetConfigById() {
        InterfaceConfig config = new InterfaceConfig();
        config.setId(1L);
        config.setConfigCode("HIS_DEPT_SYNC");
        config.setConfigName("科室信息同步");
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(mappingTableRepository.findAll()).thenReturn(Collections.emptyList());
        
        InterfaceConfigDto result = configService.getConfigById(1L);
        
        assertNotNull(result);
        assertEquals("HIS_DEPT_SYNC", result.getConfigCode());
    }

    @Test
    void testGetConfigByIdNotFound() {
        when(configRepository.findById(999L)).thenReturn(Optional.empty());
        
        InterfaceConfigDto result = configService.getConfigById(999L);
        
        assertNull(result);
    }

    @Test
    void testCreateConfig() {
        InterfaceConfigDto dto = new InterfaceConfigDto();
        dto.setConfigCode("HIS_TEST_SYNC");
        dto.setConfigName("测试接口");
        dto.setSystem("HIS");
        dto.setMode("SOAP");
        dto.setProtocol("SOAP");
        dto.setApiProtocol("HTTP");
        dto.setUrl("http://test.com/api");
        
        when(configRepository.findByConfigCode("HIS_TEST_SYNC")).thenReturn(null);
        
        InterfaceConfig saved = new InterfaceConfig();
        saved.setId(3L);
        saved.setConfigCode("HIS_TEST_SYNC");
        saved.setConfigName("测试接口");
        
        when(configRepository.save(any(InterfaceConfig.class))).thenReturn(saved);
        when(mappingTableRepository.findAll()).thenReturn(Collections.emptyList());
        
        InterfaceConfigDto result = configService.createConfig(dto);
        
        assertNotNull(result);
        assertEquals("HIS_TEST_SYNC", result.getConfigCode());
    }

    @Test
    void testCreateConfigCodeExists() {
        InterfaceConfigDto dto = new InterfaceConfigDto();
        dto.setConfigCode("HIS_DEPT_SYNC");
        dto.setConfigName("科室信息同步");
        
        when(configRepository.findByConfigCode("HIS_DEPT_SYNC")).thenReturn(new InterfaceConfig());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            configService.createConfig(dto);
        });
        
        assertTrue(exception.getMessage().contains("接口编码已存在"));
    }

    @Test
    void testUpdateConfig() {
        InterfaceConfigDto dto = new InterfaceConfigDto();
        dto.setConfigName("更新后的名称");
        
        InterfaceConfig existing = new InterfaceConfig();
        existing.setId(1L);
        existing.setConfigCode("HIS_DEPT_SYNC");
        existing.setConfigName("科室信息同步");
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(configRepository.save(any(InterfaceConfig.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mappingTableRepository.findAll()).thenReturn(Collections.emptyList());
        
        InterfaceConfigDto result = configService.updateConfig(1L, dto);
        
        assertNotNull(result);
    }

    @Test
    void testDeleteConfig() {
        InterfaceConfig config = new InterfaceConfig();
        config.setId(1L);
        
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(mappingTableRepository.findAll()).thenReturn(Collections.emptyList());
        
        assertDoesNotThrow(() -> configService.deleteConfig(1L));
        verify(configRepository).deleteById(1L);
    }

    @Test
    void testDeleteConfigNotFound() {
        when(configRepository.findById(999L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            configService.deleteConfig(999L);
        });
        
        assertTrue(exception.getMessage().contains("接口配置不存在"));
    }
}