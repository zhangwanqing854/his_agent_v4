package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Duty;
import com.hospital.handover.entity.Role;
import com.hospital.handover.entity.RoleDuty;
import com.hospital.handover.repository.DutyRepository;
import com.hospital.handover.repository.RoleDutyRepository;
import com.hospital.handover.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    private RoleRepository roleRepository;
    private DutyRepository dutyRepository;
    private RoleDutyRepository roleDutyRepository;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        dutyRepository = mock(DutyRepository.class);
        roleDutyRepository = mock(RoleDutyRepository.class);
        roleService = new RoleService(roleRepository, dutyRepository, roleDutyRepository);
    }

    @Test
    void testGetAllRoles() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setCode("SUPER_ADMIN");
        role1.setName("超级管理员");
        
        Role role2 = new Role();
        role2.setId(2L);
        role2.setCode("DOCTOR");
        role2.setName("医生");
        
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role1, role2));
        when(roleDutyRepository.findAll()).thenReturn(Collections.emptyList());
        
        var result = roleService.getAllRoles();
        
        assertEquals(2, result.size());
    }

    @Test
    void testGetRoleById() {
        Role role = new Role();
        role.setId(1L);
        role.setCode("DOCTOR");
        role.setName("医生");
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleDutyRepository.findAll()).thenReturn(Collections.emptyList());
        
        RoleDto result = roleService.getRoleById(1L);
        
        assertNotNull(result);
        assertEquals("DOCTOR", result.getCode());
    }

    @Test
    void testCreateRole() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("NURSE");
        request.setName("护士");
        
        when(roleRepository.findByCode("NURSE")).thenReturn(null);
        
        Role saved = new Role();
        saved.setId(3L);
        saved.setCode("NURSE");
        saved.setName("护士");
        
        when(roleRepository.save(any(Role.class))).thenReturn(saved);
        when(roleDutyRepository.findAll()).thenReturn(Collections.emptyList());
        
        RoleDto result = roleService.createRole(request);
        
        assertNotNull(result);
        assertEquals("NURSE", result.getCode());
    }

    @Test
    void testCreateRoleCodeExists() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("DOCTOR");
        request.setName("医生");
        
        when(roleRepository.findByCode("DOCTOR")).thenReturn(new Role());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.createRole(request);
        });
        
        assertTrue(exception.getMessage().contains("角色编码已存在"));
    }

    @Test
    void testUpdateRole() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setName("医生（更新）");
        
        Role existing = new Role();
        existing.setId(2L);
        existing.setCode("DOCTOR");
        existing.setName("医生");
        
        when(roleRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roleDutyRepository.findAll()).thenReturn(Collections.emptyList());
        
        RoleDto result = roleService.updateRole(2L, request);
        
        assertNotNull(result);
    }

    @Test
    void testUpdateSuperAdminRole() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setName("新名称");
        
        Role existing = new Role();
        existing.setId(1L);
        existing.setCode("SUPER_ADMIN");
        existing.setName("超级管理员");
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.updateRole(1L, request);
        });
        
        assertTrue(exception.getMessage().contains("无法修改超级管理员"));
    }

    @Test
    void testDeleteRole() {
        Role role = new Role();
        role.setId(3L);
        role.setCode("NURSE");
        
        when(roleRepository.findById(3L)).thenReturn(Optional.of(role));
        
        assertDoesNotThrow(() -> roleService.deleteRole(3L));
        verify(roleRepository).deleteById(3L);
    }

    @Test
    void testDeletePresetRole() {
        Role role = new Role();
        role.setId(1L);
        role.setCode("SUPER_ADMIN");
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.deleteRole(1L);
        });
        
        assertTrue(exception.getMessage().contains("无法删除预置角色"));
    }

    @Test
    void testGetDutiesByRoleId() {
        RoleDuty rd = new RoleDuty();
        rd.setRoleId(1L);
        rd.setDutyId(1L);
        
        Duty duty = new Duty();
        duty.setId(1L);
        duty.setCode("HANDOVER_MANAGE");
        duty.setName("交班管理");
        
        when(roleDutyRepository.findAll()).thenReturn(Arrays.asList(rd));
        when(dutyRepository.findById(1L)).thenReturn(Optional.of(duty));
        
        var result = roleService.getDutiesByRoleId(1L);
        
        assertEquals(1, result.size());
        assertEquals("交班管理", result.get(0).getName());
    }
}