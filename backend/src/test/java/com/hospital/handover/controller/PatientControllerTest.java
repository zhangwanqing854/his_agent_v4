package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientControllerTest {

    private PatientService patientService;
    private PatientController patientController;

    @BeforeEach
    void setUp() {
        patientService = mock(PatientService.class);
        patientController = new PatientController(patientService);
    }

    @Test
    void testGetPatientList() {
        VisitDto visit = new VisitDto();
        visit.setId(1L);
        visit.setPatientName("张三");
        
        when(patientService.getPatientList(any())).thenReturn(Arrays.asList(visit));
        
        ResponseEntity<ApiResponse<java.util.List<VisitDto>>> response = 
            patientController.getPatientList(null, null, null, null);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetPatientListWithFilter() {
        VisitDto visit = new VisitDto();
        visit.setId(1L);
        visit.setPatientName("张三");
        visit.setVisitStatus("INPATIENT");
        
        when(patientService.getPatientList(any())).thenReturn(Arrays.asList(visit));
        
        ResponseEntity<ApiResponse<java.util.List<VisitDto>>> response = 
            patientController.getPatientList(1L, "张", null, "INPATIENT");
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testGetPatientById() {
        PatientDto patient = new PatientDto();
        patient.setId(1L);
        patient.setName("张三");
        
        when(patientService.getPatientById(1L)).thenReturn(patient);
        
        ResponseEntity<ApiResponse<PatientDto>> response = patientController.getPatientById(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("张三", response.getBody().getData().getName());
    }

    @Test
    void testGetPatientByIdNotFound() {
        when(patientService.getPatientById(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<PatientDto>> response = patientController.getPatientById(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    void testGetVisitByPatientId() {
        VisitDto visit = new VisitDto();
        visit.setId(1L);
        visit.setVisitNo("V001");
        visit.setPatientName("张三");
        
        when(patientService.getVisitByPatientId(1L)).thenReturn(visit);
        
        ResponseEntity<ApiResponse<VisitDto>> response = patientController.getVisitByPatientId(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("V001", response.getBody().getData().getVisitNo());
    }

    @Test
    void testGetVisitByPatientIdNotFound() {
        when(patientService.getVisitByPatientId(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<VisitDto>> response = patientController.getVisitByPatientId(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
    }
}