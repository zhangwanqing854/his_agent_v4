package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.AuthService;
import com.hospital.handover.config.JwtProperties;
import com.hospital.handover.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private JwtUtil jwtUtil;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("handover-system-secret-key-very-long-for-security-2024");
        jwtProperties.setExpiration(86400000);
        jwtUtil = new JwtUtil(jwtProperties);
        
        authController = new AuthController(authService, jwtUtil);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("admin", "admin");
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("admin");
        userInfo.setName("张医生");
        userInfo.setRole("doctor");
        
        LoginResponse loginResponse = new LoginResponse("test-token", userInfo);
        
        when(authService.login(request)).thenReturn(loginResponse);
        
        ResponseEntity<ApiResponse<LoginResponse>> response = authController.login(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("登录成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testLoginFailed() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");
        
        when(authService.login(request)).thenThrow(new RuntimeException("用户名或密码错误"));
        
        ResponseEntity<ApiResponse<LoginResponse>> response = authController.login(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(401, response.getBody().getCode());
        assertEquals("用户名或密码错误", response.getBody().getMessage());
    }

    @Test
    void testLogout() {
        ResponseEntity<ApiResponse<Void>> response = authController.logout();
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals("退出成功", response.getBody().getMessage());
    }

    @Test
    void testGetCurrentUserSuccess() {
        String token = jwtUtil.generateToken(1L, "admin", 100L);
        String authHeader = "Bearer " + token;
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("admin");
        userInfo.setName("张医生");
        userInfo.setRole("doctor");
        
        DepartmentInfo dept = new DepartmentInfo(100L, "XNK", "心内科", true);
        userInfo.setDepartments(Arrays.asList(dept));
        
        when(authService.getCurrentUser(1L)).thenReturn(userInfo);
        
        ResponseEntity<ApiResponse<UserInfo>> response = authController.getCurrentUser(authHeader);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertNotNull(response.getBody().getData());
        assertEquals("admin", response.getBody().getData().getUsername());
    }

    @Test
    void testGetCurrentUserNoToken() {
        ResponseEntity<ApiResponse<UserInfo>> response = authController.getCurrentUser(null);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(401, response.getBody().getCode());
        assertEquals("未授权", response.getBody().getMessage());
    }

    @Test
    void testGetCurrentUserInvalidToken() {
        String authHeader = "Bearer invalid-token";
        
        ResponseEntity<ApiResponse<UserInfo>> response = authController.getCurrentUser(authHeader);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(401, response.getBody().getCode());
        assertEquals("Token无效或已过期", response.getBody().getMessage());
    }

    @Test
    void testGetCurrentUserWithoutBearerPrefix() {
        String authHeader = "test-token";
        
        ResponseEntity<ApiResponse<UserInfo>> response = authController.getCurrentUser(authHeader);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(401, response.getBody().getCode());
        assertEquals("未授权", response.getBody().getMessage());
    }
}