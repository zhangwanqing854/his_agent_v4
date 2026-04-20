package com.hospital.handover.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.handover.client.HisSoapClient;
import com.hospital.handover.entity.VitalSign;
import com.hospital.handover.repository.VitalSignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;

class VitalSignSyncServiceTest {

    private HisSoapClient hisSoapClient;
    private VitalSignRepository vitalSignRepository;
    private VitalSignSyncService vitalSignSyncService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        hisSoapClient = mock(HisSoapClient.class);
        vitalSignRepository = mock(VitalSignRepository.class);
        objectMapper = new ObjectMapper();
        vitalSignSyncService = new VitalSignSyncService(hisSoapClient, vitalSignRepository);
    }

    @Test
    void testSyncVitalSignsSuccess() throws Exception {
        String jsonData = "[{\"code_pat\":\"000221711800\",\"name_vs_item\":\"体温\",\"value_vs_item\":\"37.1\",\"unit_vs_item\":\"℃\",\"record_time\":\"2026-04-15 10:00:00\"}]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetEntVt(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(response);
        when(vitalSignRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        
        LocalDateTime startTime = LocalDateTime.now().minusHours(6);
        LocalDateTime endTime = LocalDateTime.now();
        
        VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(startTime, endTime);
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getSkipCount());
    }

    @Test
    void testSyncVitalSignsDeleteBeforeInsert() throws Exception {
        String jsonData = "[{\"code_pat\":\"000221711800\",\"name_vs_item\":\"体温\",\"value_vs_item\":\"37.1\",\"unit_vs_item\":\"℃\",\"record_time\":\"2026-04-15 10:00:00\"},{\"code_pat\":\"000221711801\",\"name_vs_item\":\"脉搏\",\"value_vs_item\":\"72\",\"unit_vs_item\":\"次/分\",\"record_time\":\"2026-04-15 10:00:00\"}]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetEntVt(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(response);
        when(vitalSignRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        
        VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(LocalDateTime.now().minusHours(6), LocalDateTime.now());
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(2, result.getSuccessCount());
        
        verify(vitalSignRepository).deleteByPatientNoIn(anyList());
        verify(vitalSignRepository).saveAll(anyList());
        
        InOrder inOrder = inOrder(vitalSignRepository);
        inOrder.verify(vitalSignRepository).deleteByPatientNoIn(anyList());
        inOrder.verify(vitalSignRepository).saveAll(anyList());
    }

    @Test
    void testSyncVitalSignsMultipleRecordsSamePatient() throws Exception {
        String jsonData = "[{\"code_pat\":\"000221711800\",\"name_vs_item\":\"体温\",\"value_vs_item\":\"37.1\",\"unit_vs_item\":\"℃\",\"record_time\":\"2026-04-15 10:00:00\"},{\"code_pat\":\"000221711800\",\"name_vs_item\":\"脉搏\",\"value_vs_item\":\"72\",\"unit_vs_item\":\"次/分\",\"record_time\":\"2026-04-15 10:00:00\"}]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetEntVt(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(response);
        when(vitalSignRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        
        VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(LocalDateTime.now().minusHours(6), LocalDateTime.now());
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(2, result.getSuccessCount());
        
        verify(vitalSignRepository).deleteByPatientNoIn(List.of("000221711800"));
    }

    @Test
    void testSyncVitalSignsWithMissingFields() throws Exception {
        String jsonData = "[{\"code_pat\":\"000221711800\",\"name_vs_item\":\"体温\",\"value_vs_item\":\"37.1\"},{\"name_vs_item\":\"心率\"}]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetEntVt(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(response);
        when(vitalSignRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        
        VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(LocalDateTime.now().minusHours(6), LocalDateTime.now());
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getSkipCount());
    }

    @Test
    void testSyncVitalSignsWithSoapException() throws Exception {
        when(hisSoapClient.callGetEntVt(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenThrow(new Exception("SOAP调用失败"));
        
        VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(LocalDateTime.now().minusHours(6), LocalDateTime.now());
        
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getMessage().contains("SOAP调用失败"));
    }

    @Test
    void testSyncVitalSignsWithEmptyResponse() throws Exception {
        String jsonData = "[]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetEntVt(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(response);
        
        VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(LocalDateTime.now().minusHours(6), LocalDateTime.now());
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(0, result.getSuccessCount());
    }

    @Test
    void testSyncVitalSignsWithInvalidResponseFormat() throws Exception {
        String jsonData = "{\"data\":\"invalid\"}";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetEntVt(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(response);
        
        VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(LocalDateTime.now().minusHours(6), LocalDateTime.now());
        
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getMessage().contains("格式异常"));
    }
}