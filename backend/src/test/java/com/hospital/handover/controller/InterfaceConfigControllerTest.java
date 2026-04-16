package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.InterfaceConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterfaceConfigControllerTest {

    private InterfaceConfigService configService;
    private InterfaceConfigController configController;

    @BeforeEach
    void setUp() {
        configService = mock(InterfaceConfigService.class);
        configController = new InterfaceConfigController(configService);
    }

    @Test
    void testGetAllConfigs() {
        InterfaceConfigDto config = new InterfaceConfigDto();
        config.setId(1L);
        config.setConfigCode("HIS_DEPT_SYNC");
        
        when(configService.getAllConfigs()).thenReturn(Arrays.asList(config));
        
        ResponseEntity<ApiResponse<java.util.List<InterfaceConfigDto>>> response = 
            configController.getAllConfigs();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetConfigById() {
        InterfaceConfigDto config = new InterfaceConfigDto();
        config.setId(1L);
        config.setConfigCode("HIS_DEPT_SYNC");
        config.setConfigName("科室信息同步");
        
        when(configService.getConfigById(1L)).thenReturn(config);
        
        ResponseEntity<ApiResponse<InterfaceConfigDto>> response = configController.getConfigById(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("科室信息同步", response.getBody().getData().getConfigName());
    }

    @Test
    void testGetConfigByIdNotFound() {
        when(configService.getConfigById(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<InterfaceConfigDto>> response = configController.getConfigById(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    void testCreateConfig() {
        InterfaceConfigDto dto = new InterfaceConfigDto();
        dto.setConfigCode("HIS_TEST_SYNC");
        dto.setConfigName("测试接口");
        
        InterfaceConfigDto created = new InterfaceConfigDto();
        created.setId(3L);
        created.setConfigCode("HIS_TEST_SYNC");
        
        when(configService.createConfig(dto)).thenReturn(created);
        
        ResponseEntity<ApiResponse<InterfaceConfigDto>> response = configController.createConfig(dto);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testUpdateConfig() {
        InterfaceConfigDto dto = new InterfaceConfigDto();
        dto.setConfigName("更新后的名称");
        
        InterfaceConfigDto updated = new InterfaceConfigDto();
        updated.setId(1L);
        updated.setConfigName("更新后的名称");
        
        when(configService.updateConfig(1L, dto)).thenReturn(updated);
        
        ResponseEntity<ApiResponse<InterfaceConfigDto>> response = configController.updateConfig(1L, dto);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testDeleteConfig() {
        doNothing().when(configService).deleteConfig(1L);
        
        ResponseEntity<ApiResponse<Void>> response = configController.deleteConfig(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testTestConnection() {
        InterfaceConfigDto config = new InterfaceConfigDto();
        config.setId(1L);
        config.setConfigCode("HIS_DEPT_SYNC");
        
        when(configService.getConfigById(1L)).thenReturn(config);
        
        ResponseEntity<ApiResponse<TestResultDto>> response = configController.testConnection(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getData().getSuccess());
    }
}