package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Role;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.RoleRepository;
import com.hospital.handover.repository.UserRepository;
import com.hospital.handover.config.JwtProperties;
import com.hospital.handover.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private DepartmentRepository departmentRepository;
    private DoctorDepartmentRepository doctorDepartmentRepository;
    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        doctorDepartmentRepository = mock(DoctorDepartmentRepository.class);
        
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("handover-system-secret-key-very-long-for-security-2024");
        jwtProperties.setExpiration(86400000);
        jwtUtil = new JwtUtil(jwtProperties);
        
        authService = new AuthService(
            userRepository, 
            roleRepository, 
            departmentRepository, 
            doctorDepartmentRepository, 
            jwtUtil
        );
    }

    @Test
    void testLoginSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEnabled(true);
        user.setRoleId(1L);
        
        Role role = new Role();
        role.setId(1L);
        role.setCode("DOCTOR");
        
        when(userRepository.findByUsername("admin")).thenReturn(user);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        
        LoginRequest request = new LoginRequest("admin", "admin");
        LoginResponse response = authService.login(request);
        
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("admin", response.getUserInfo().getUsername());
        
        assertTrue(jwtUtil.validateToken(response.getToken()));
        assertEquals(1L, jwtUtil.getUserIdFromToken(response.getToken()));
    }

    @Test
    void testLoginUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);
        
        LoginRequest request = new LoginRequest("nonexistent", "password");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    void testLoginUserDisabled() {
        User user = new User();
        user.setId(1L);
        user.setUsername("disabled");
        user.setPassword("password");
        user.setEnabled(false);
        
        when(userRepository.findByUsername("disabled")).thenReturn(user);
        
        LoginRequest request = new LoginRequest("disabled", "password");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        
        assertTrue(exception.getMessage().contains("账号已被禁用"));
    }

    @Test
    void testLoginWrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("correctpassword");
        user.setEnabled(true);
        
        when(userRepository.findByUsername("admin")).thenReturn(user);
        
        LoginRequest request = new LoginRequest("admin", "wrongpassword");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    void testGetCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setRoleId(1L);
        
        Role role = new Role();
        role.setId(1L);
        role.setCode("DOCTOR");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        
        UserInfo userInfo = authService.getCurrentUser(1L);
        
        assertNotNull(userInfo);
        assertEquals(1L, userInfo.getId());
        assertEquals("admin", userInfo.getUsername());
        assertEquals("doctor", userInfo.getRole());
    }

    @Test
    void testGetCurrentUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser(999L);
        });
        
        assertTrue(exception.getMessage().contains("用户不存在"));
    }
}