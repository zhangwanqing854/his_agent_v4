package com.hospital.handover.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.handover.client.HisSoapClient;
import com.hospital.handover.entity.DeptPatientOverview;
import com.hospital.handover.repository.DeptPatientOverviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DeptPatientInfoSyncServiceTest {

    private HisSoapClient hisSoapClient;
    private DeptPatientOverviewRepository deptPatientOverviewRepository;
    private DeptPatientInfoSyncService deptPatientInfoSyncService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        hisSoapClient = mock(HisSoapClient.class);
        deptPatientOverviewRepository = mock(DeptPatientOverviewRepository.class);
        objectMapper = new ObjectMapper();
        deptPatientInfoSyncService = new DeptPatientInfoSyncService(hisSoapClient, deptPatientOverviewRepository);
    }

    @Test
    void testSyncDeptPatientInfoSuccess() throws Exception {
        String jsonData = "[{\"code_dept\":\"0000094\",\"id_dept\":\"0001Z8100000000LJ1N4\",\"totalNum\":86,\"newInHos\":0,\"diseNum\":10}]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetDeptPatientInfo(anyString())).thenReturn(response);
        when(deptPatientOverviewRepository.findByDeptCode(anyString())).thenReturn(Optional.empty());
        when(deptPatientOverviewRepository.save(any(DeptPatientOverview.class))).thenAnswer(inv -> inv.getArgument(0));
        
        DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo(null);
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getSkipCount());
    }

    @Test
    void testSyncDeptPatientInfoUpdateExisting() throws Exception {
        String jsonData = "[{\"code_dept\":\"0000094\",\"id_dept\":\"0001Z8100000000LJ1N4\",\"totalNum\":90,\"newInHos\":5}]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        DeptPatientOverview existing = new DeptPatientOverview();
        existing.setDeptCode("0000094");
        existing.setTotalNum(86);
        
        when(hisSoapClient.callGetDeptPatientInfo(anyString())).thenReturn(response);
        when(deptPatientOverviewRepository.findByDeptCode("0000094")).thenReturn(Optional.of(existing));
        when(deptPatientOverviewRepository.save(any(DeptPatientOverview.class))).thenAnswer(inv -> inv.getArgument(0));
        
        DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo("0000094");
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getSuccessCount());
        verify(deptPatientOverviewRepository).findByDeptCode("0000094");
    }

    @Test
    void testSyncDeptPatientInfoWithMissingDeptCode() throws Exception {
        String jsonData = "[{\"totalNum\":86},{\"code_dept\":\"0000094\",\"totalNum\":90}]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetDeptPatientInfo(anyString())).thenReturn(response);
        when(deptPatientOverviewRepository.findByDeptCode(anyString())).thenReturn(Optional.empty());
        when(deptPatientOverviewRepository.save(any(DeptPatientOverview.class))).thenAnswer(inv -> inv.getArgument(0));
        
        DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo(null);
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getSkipCount());
    }

    @Test
    void testSyncDeptPatientInfoWithSoapException() throws Exception {
        when(hisSoapClient.callGetDeptPatientInfo(anyString()))
            .thenThrow(new Exception("SOAP调用失败"));
        
        DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo(null);
        
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getMessage().contains("SOAP调用失败"));
    }

    @Test
    void testSyncDeptPatientInfoWithEmptyResponse() throws Exception {
        String jsonData = "[]";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetDeptPatientInfo(anyString())).thenReturn(response);
        
        DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo(null);
        
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(0, result.getSuccessCount());
    }

    @Test
    void testSyncDeptPatientInfoWithInvalidResponseFormat() throws Exception {
        String jsonData = "{\"data\":\"invalid\"}";
        JsonNode response = objectMapper.readTree(jsonData);
        
        when(hisSoapClient.callGetDeptPatientInfo(anyString())).thenReturn(response);
        
        DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo(null);
        
        assertEquals("FAILED", result.getStatus());
        assertTrue(result.getMessage().contains("格式异常"));
    }
}