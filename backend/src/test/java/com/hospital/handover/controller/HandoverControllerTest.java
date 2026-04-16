package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.HandoverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandoverControllerTest {

    private HandoverService handoverService;
    private HandoverController handoverController;

    @BeforeEach
    void setUp() {
        handoverService = mock(HandoverService.class);
        handoverController = new HandoverController(handoverService);
    }

    @Test
    void testGetHandoverList() {
        HandoverDto handover = new HandoverDto();
        handover.setId(1L);
        handover.setDeptName("心内科");
        handover.setShift("白班");
        
        when(handoverService.getHandoverList(1L)).thenReturn(Arrays.asList(handover));
        
        ResponseEntity<ApiResponse<java.util.List<HandoverDto>>> response = 
            handoverController.getHandoverList(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("心内科", response.getBody().getData().get(0).getDeptName());
    }

    @Test
    void testGetHandoverById() {
        HandoverDto handover = new HandoverDto();
        handover.setId(1L);
        handover.setDeptName("心内科");
        handover.setShift("白班");
        handover.setPatientCount(5);
        
        when(handoverService.getHandoverById(1L)).thenReturn(handover);
        
        ResponseEntity<ApiResponse<HandoverDto>> response = handoverController.getHandoverById(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("心内科", response.getBody().getData().getDeptName());
        assertEquals(5, response.getBody().getData().getPatientCount());
    }

    @Test
    void testGetHandoverByIdNotFound() {
        when(handoverService.getHandoverById(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<HandoverDto>> response = handoverController.getHandoverById(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
        assertEquals("交班记录不存在", response.getBody().getMessage());
    }

    @Test
    void testGetHandoverPatients() {
        HandoverPatientDto patient = new HandoverPatientDto();
        patient.setId(1L);
        patient.setPatientName("张三");
        patient.setBedNo("A101");
        patient.setFilterReason("24小时内新入院");
        patient.setCurrentCondition("药物治疗: 王医生");
        
        when(handoverService.getHandoverPatients(1L)).thenReturn(Arrays.asList(patient));
        
        ResponseEntity<ApiResponse<java.util.List<HandoverPatientDto>>> response = 
            handoverController.getHandoverPatients(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("张三", response.getBody().getData().get(0).getPatientName());
        assertEquals("24小时内新入院", response.getBody().getData().get(0).getFilterReason());
    }

    @Test
    void testGetHandoverPatientsEmpty() {
        when(handoverService.getHandoverPatients(1L)).thenReturn(Collections.emptyList());
        
        ResponseEntity<ApiResponse<java.util.List<HandoverPatientDto>>> response = 
            handoverController.getHandoverPatients(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getData().size());
    }

    @Test
    void testCreateHandover() {
        HandoverDto handover = new HandoverDto();
        handover.setId(1L);
        handover.setDeptName("心内科");
        handover.setShift("白班");
        handover.setPatientCount(3);
        
        HandoverCreateRequest request = new HandoverCreateRequest();
        request.setDeptId(1L);
        request.setHandoverDate(LocalDate.now());
        request.setShift("白班");
        request.setFromDoctorId(1L);
        
        when(handoverService.createHandover(request)).thenReturn(handover);
        
        ResponseEntity<ApiResponse<HandoverDto>> response = handoverController.createHandover(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("心内科", response.getBody().getData().getDeptName());
        assertEquals(3, response.getBody().getData().getPatientCount());
    }

    @Test
    void testCreateHandoverDeptNotFound() {
        HandoverCreateRequest request = new HandoverCreateRequest();
        request.setDeptId(999L);
        request.setHandoverDate(LocalDate.now());
        request.setShift("白班");
        request.setFromDoctorId(1L);
        
        when(handoverService.createHandover(request)).thenReturn(null);
        
        ResponseEntity<ApiResponse<HandoverDto>> response = handoverController.createHandover(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(400, response.getBody().getCode());
        assertEquals("科室不存在", response.getBody().getMessage());
    }
}