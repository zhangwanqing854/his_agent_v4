package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Duty;
import com.hospital.handover.entity.Permission;
import com.hospital.handover.repository.DutyRepository;
import com.hospital.handover.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionServiceTest {

    private PermissionRepository permissionRepository;
    private DutyRepository dutyRepository;
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionRepository = mock(PermissionRepository.class);
        dutyRepository = mock(DutyRepository.class);
        permissionService = new PermissionService(permissionRepository, dutyRepository);
    }

    @Test
    void testGetAllPermissions() {
        Permission perm1 = new Permission();
        perm1.setId(1L);
        perm1.setCode("HANDOVER");
        perm1.setName("交班管理");
        
        Permission perm2 = new Permission();
        perm2.setId(2L);
        perm2.setCode("PATIENT");
        perm2.setName("患者管理");
        
        when(permissionRepository.findAll()).thenReturn(Arrays.asList(perm1, perm2));
        
        var result = permissionService.getAllPermissions();
        
        assertEquals(2, result.size());
    }

    @Test
    void testGetPermissionById() {
        Permission perm = new Permission();
        perm.setId(1L);
        perm.setCode("HANDOVER");
        perm.setName("交班管理");
        
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(perm));
        
        PermissionDto result = permissionService.getPermissionById(1L);
        
        assertNotNull(result);
        assertEquals("交班管理", result.getName());
    }

    @Test
    void testGetDutiesByPermissionId() {
        Duty duty1 = new Duty();
        duty1.setId(1L);
        duty1.setCode("HANDOVER_CREATE");
        duty1.setName("创建交班");
        duty1.setPermissionId(1L);
        
        Duty duty2 = new Duty();
        duty2.setId(2L);
        duty2.setCode("HANDOVER_VIEW");
        duty2.setName("查看交班");
        duty2.setPermissionId(1L);
        
        Duty duty3 = new Duty();
        duty3.setId(3L);
        duty3.setCode("PATIENT_VIEW");
        duty3.setName("查看患者");
        duty3.setPermissionId(2L);
        
        when(dutyRepository.findAll()).thenReturn(Arrays.asList(duty1, duty2, duty3));
        
        Permission perm = new Permission();
        perm.setId(1L);
        perm.setName("交班管理");
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(perm));
        
        var result = permissionService.getDutiesByPermissionId(1L);
        
        assertEquals(2, result.size());
    }
}