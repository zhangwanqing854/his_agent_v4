package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.HisStaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HisStaffControllerTest {

    private HisStaffService hisStaffService;
    private HisStaffController hisStaffController;

    @BeforeEach
    void setUp() {
        hisStaffService = mock(HisStaffService.class);
        hisStaffController = new HisStaffController(hisStaffService);
    }

    @Test
    void testGetAllStaff() {
        HisStaffDto staff = new HisStaffDto();
        staff.setId(1L);
        staff.setName("张医生");
        
        when(hisStaffService.getAllStaff()).thenReturn(Arrays.asList(staff));
        
        ResponseEntity<ApiResponse<java.util.List<HisStaffDto>>> response = 
            hisStaffController.getAllStaff(null);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetStaffByDepartment() {
        HisStaffDto staff = new HisStaffDto();
        staff.setId(1L);
        staff.setName("张医生");
        staff.setDepartmentId(1L);
        
        when(hisStaffService.getStaffByDepartment(1L)).thenReturn(Arrays.asList(staff));
        
        ResponseEntity<ApiResponse<java.util.List<HisStaffDto>>> response = 
            hisStaffController.getAllStaff(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetStaffById() {
        HisStaffDto staff = new HisStaffDto();
        staff.setId(1L);
        staff.setName("张医生");
        staff.setStaffCode("S001");
        
        when(hisStaffService.getStaffById(1L)).thenReturn(staff);
        
        ResponseEntity<ApiResponse<HisStaffDto>> response = hisStaffController.getStaffById(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("张医生", response.getBody().getData().getName());
    }

    @Test
    void testGetStaffByIdNotFound() {
        when(hisStaffService.getStaffById(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<HisStaffDto>> response = hisStaffController.getStaffById(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    void testGetUnlinkedStaff() {
        HisStaffDto staff = new HisStaffDto();
        staff.setId(2L);
        staff.setName("李医生");
        
        when(hisStaffService.getUnlinkedStaff()).thenReturn(Arrays.asList(staff));
        
        ResponseEntity<ApiResponse<java.util.List<HisStaffDto>>> response = 
            hisStaffController.getUnlinkedStaff();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testCreateStaff() {
        HisStaffCreateRequest request = new HisStaffCreateRequest();
        request.setStaffCode("S001");
        request.setName("张医生");
        
        HisStaffDto created = new HisStaffDto();
        created.setId(1L);
        created.setStaffCode("S001");
        created.setName("张医生");
        
        when(hisStaffService.createStaff(request)).thenReturn(created);
        
        ResponseEntity<ApiResponse<HisStaffDto>> response = hisStaffController.createStaff(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("张医生", response.getBody().getData().getName());
    }

    @Test
    void testCreateStaffDuplicateCode() {
        HisStaffCreateRequest request = new HisStaffCreateRequest();
        request.setStaffCode("S001");
        request.setName("张医生");
        
        when(hisStaffService.createStaff(request)).thenThrow(new RuntimeException("员工编码已存在"));
        
        ResponseEntity<ApiResponse<HisStaffDto>> response = hisStaffController.createStaff(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("员工编码已存在"));
    }

    @Test
    void testUpdateStaff() {
        HisStaffUpdateRequest request = new HisStaffUpdateRequest();
        request.setName("张医生(更新)");
        
        HisStaffDto updated = new HisStaffDto();
        updated.setId(1L);
        updated.setName("张医生(更新)");
        
        when(hisStaffService.updateStaff(1L, request)).thenReturn(updated);
        
        ResponseEntity<ApiResponse<HisStaffDto>> response = hisStaffController.updateStaff(1L, request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("张医生(更新)", response.getBody().getData().getName());
    }

    @Test
    void testDeleteStaff() {
        doNothing().when(hisStaffService).deleteStaff(1L);
        
        ResponseEntity<ApiResponse<Void>> response = hisStaffController.deleteStaff(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testDeleteStaffLinked() {
        doThrow(new RuntimeException("该员工已关联用户账号，无法删除"))
            .when(hisStaffService).deleteStaff(1L);
        
        ResponseEntity<ApiResponse<Void>> response = hisStaffController.deleteStaff(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("已关联"));
    }
}