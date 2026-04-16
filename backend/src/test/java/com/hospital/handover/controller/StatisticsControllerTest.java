package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.StatisticsDto;
import com.hospital.handover.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsControllerTest {

    private StatisticsService statisticsService;
    private StatisticsController statisticsController;

    @BeforeEach
    void setUp() {
        statisticsService = mock(StatisticsService.class);
        statisticsController = new StatisticsController(statisticsService);
    }

    @Test
    void testGetDepartmentStatistics() {
        StatisticsDto stats = new StatisticsDto();
        stats.setDeptId(1L);
        stats.setDeptName("心内科");
        stats.setTotalHandovers(10);
        stats.setCompletedHandovers(8);
        stats.setDraftHandovers(2);
        stats.setTotalPatients(50);
        stats.setNewAdmissionPatients(5);
        stats.setLevel1NursingPatients(3);
        
        when(statisticsService.getDepartmentStatistics(1L)).thenReturn(stats);
        
        ResponseEntity<ApiResponse<StatisticsDto>> response = 
            statisticsController.getDepartmentStatistics(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("心内科", response.getBody().getData().getDeptName());
        assertEquals(10, response.getBody().getData().getTotalHandovers());
        assertEquals(8, response.getBody().getData().getCompletedHandovers());
        assertEquals(2, response.getBody().getData().getDraftHandovers());
    }

    @Test
    void testGetDepartmentStatisticsNotFound() {
        when(statisticsService.getDepartmentStatistics(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<StatisticsDto>> response = 
            statisticsController.getDepartmentStatistics(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
        assertEquals("科室不存在", response.getBody().getMessage());
    }

    @Test
    void testGetAllDepartmentStatistics() {
        StatisticsDto stats1 = new StatisticsDto();
        stats1.setDeptId(1L);
        stats1.setDeptName("心内科");
        stats1.setTotalHandovers(10);
        
        StatisticsDto stats2 = new StatisticsDto();
        stats2.setDeptId(2L);
        stats2.setDeptName("神经内科");
        stats2.setTotalHandovers(5);
        
        when(statisticsService.getAllDepartmentStatistics())
            .thenReturn(Arrays.asList(stats1, stats2));
        
        ResponseEntity<ApiResponse<java.util.List<StatisticsDto>>> response = 
            statisticsController.getAllDepartmentStatistics();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("心内科", response.getBody().getData().get(0).getDeptName());
        assertEquals("神经内科", response.getBody().getData().get(1).getDeptName());
    }

    @Test
    void testGetAllDepartmentStatisticsEmpty() {
        when(statisticsService.getAllDepartmentStatistics())
            .thenReturn(Collections.emptyList());
        
        ResponseEntity<ApiResponse<java.util.List<StatisticsDto>>> response = 
            statisticsController.getAllDepartmentStatistics();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getData().size());
    }
}