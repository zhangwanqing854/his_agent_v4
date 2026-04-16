package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentControllerTest {

    private DepartmentService departmentService;
    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        departmentService = mock(DepartmentService.class);
        departmentController = new DepartmentController(departmentService);
    }

    @Test
    void testGetAllDepartments() {
        DepartmentDto dept = new DepartmentDto(1L, "XNK", "心内科", 30);
        when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(dept));
        
        ResponseEntity<ApiResponse<java.util.List<DepartmentDto>>> response = 
            departmentController.getAllDepartments();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetDepartmentById() {
        DepartmentDto dept = new DepartmentDto(1L, "XNK", "心内科", 30);
        when(departmentService.getDepartmentById(1L)).thenReturn(dept);
        
        ResponseEntity<ApiResponse<DepartmentDto>> response = 
            departmentController.getDepartmentById(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("心内科", response.getBody().getData().getName());
    }

    @Test
    void testGetDepartmentByIdNotFound() {
        when(departmentService.getDepartmentById(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<DepartmentDto>> response = 
            departmentController.getDepartmentById(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    void testCreateDepartment() {
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setCode("HXK");
        request.setName("呼吸科");
        
        DepartmentDto created = new DepartmentDto(3L, "HXK", "呼吸科", 0);
        when(departmentService.createDepartment(request)).thenReturn(created);
        
        ResponseEntity<ApiResponse<DepartmentDto>> response = 
            departmentController.createDepartment(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("呼吸科", response.getBody().getData().getName());
    }

    @Test
    void testCreateDepartmentCodeExists() {
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setCode("XNK");
        request.setName("心内科");
        
        when(departmentService.createDepartment(request))
            .thenThrow(new RuntimeException("科室编码已存在"));
        
        ResponseEntity<ApiResponse<DepartmentDto>> response = 
            departmentController.createDepartment(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("科室编码已存在"));
    }

    @Test
    void testUpdateDepartment() {
        DepartmentUpdateRequest request = new DepartmentUpdateRequest();
        request.setName("心内科（更新）");
        
        DepartmentDto updated = new DepartmentDto(1L, "XNK", "心内科（更新）", 30);
        when(departmentService.updateDepartment(1L, request)).thenReturn(updated);
        
        ResponseEntity<ApiResponse<DepartmentDto>> response = 
            departmentController.updateDepartment(1L, request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("心内科（更新）", response.getBody().getData().getName());
    }

    @Test
    void testDeleteDepartment() {
        doNothing().when(departmentService).deleteDepartment(1L);
        
        ResponseEntity<ApiResponse<Void>> response = 
            departmentController.deleteDepartment(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("删除成功", response.getBody().getMessage());
    }

    @Test
    void testDeleteDepartmentNotFound() {
        doThrow(new RuntimeException("科室不存在"))
            .when(departmentService).deleteDepartment(999L);
        
        ResponseEntity<ApiResponse<Void>> response = 
            departmentController.deleteDepartment(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getCode());
    }
}