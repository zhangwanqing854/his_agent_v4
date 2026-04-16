package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {

    private DepartmentRepository departmentRepository;
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentRepository = mock(DepartmentRepository.class);
        departmentService = new DepartmentService(departmentRepository);
    }

    @Test
    void testGetAllDepartments() {
        Department dept1 = new Department();
        dept1.setId(1L);
        dept1.setCode("XNK");
        dept1.setName("心内科");
        dept1.setBedCount(30);
        
        Department dept2 = new Department();
        dept2.setId(2L);
        dept2.setCode("SJNK");
        dept2.setName("神经内科");
        dept2.setBedCount(25);
        
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(dept1, dept2));
        
        var result = departmentService.getAllDepartments();
        
        assertEquals(2, result.size());
        assertEquals("XNK", result.get(0).getCode());
        assertEquals("心内科", result.get(0).getName());
    }

    @Test
    void testGetDepartmentById() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setCode("XNK");
        dept.setName("心内科");
        dept.setBedCount(30);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        
        DepartmentDto result = departmentService.getDepartmentById(1L);
        
        assertNotNull(result);
        assertEquals("XNK", result.getCode());
        assertEquals("心内科", result.getName());
    }

    @Test
    void testGetDepartmentByIdNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        
        DepartmentDto result = departmentService.getDepartmentById(999L);
        
        assertNull(result);
    }

    @Test
    void testGetDepartmentByCode() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setCode("XNK");
        dept.setName("心内科");
        
        when(departmentRepository.findByCode("XNK")).thenReturn(Optional.of(dept));
        
        DepartmentDto result = departmentService.getDepartmentByCode("XNK");
        
        assertNotNull(result);
        assertEquals("XNK", result.getCode());
    }

    @Test
    void testCreateDepartment() {
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setCode("HXK");
        request.setName("呼吸科");
        request.setBedCount(20);
        
        when(departmentRepository.findByCode("HXK")).thenReturn(Optional.empty());
        
        Department saved = new Department();
        saved.setId(3L);
        saved.setCode("HXK");
        saved.setName("呼吸科");
        saved.setBedCount(20);
        
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);
        
        DepartmentDto result = departmentService.createDepartment(request);
        
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("HXK", result.getCode());
        assertEquals("呼吸科", result.getName());
    }

    @Test
    void testCreateDepartmentCodeExists() {
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setCode("XNK");
        request.setName("心内科");
        
        when(departmentRepository.findByCode("XNK")).thenReturn(Optional.of(new Department()));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            departmentService.createDepartment(request);
        });
        
        assertTrue(exception.getMessage().contains("科室编码已存在"));
    }

    @Test
    void testUpdateDepartment() {
        DepartmentUpdateRequest request = new DepartmentUpdateRequest();
        request.setName("心内科（更新）");
        request.setBedCount(35);
        
        Department existing = new Department();
        existing.setId(1L);
        existing.setCode("XNK");
        existing.setName("心内科");
        existing.setBedCount(30);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));
        
        DepartmentDto result = departmentService.updateDepartment(1L, request);
        
        assertNotNull(result);
        assertEquals("心内科（更新）", result.getName());
        assertEquals(35, result.getBedCount());
    }

    @Test
    void testUpdateDepartmentNotFound() {
        DepartmentUpdateRequest request = new DepartmentUpdateRequest();
        request.setName("测试");
        
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            departmentService.updateDepartment(999L, request);
        });
        
        assertTrue(exception.getMessage().contains("科室不存在"));
    }

    @Test
    void testDeleteDepartment() {
        Department existing = new Department();
        existing.setId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        
        assertDoesNotThrow(() -> departmentService.deleteDepartment(1L));
        
        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void testDeleteDepartmentNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            departmentService.deleteDepartment(999L);
        });
        
        assertTrue(exception.getMessage().contains("科室不存在"));
    }
}