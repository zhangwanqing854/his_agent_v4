package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void testGetUserList() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setUsername("admin");
        
        when(userService.getUserList(any())).thenReturn(Arrays.asList(user));
        
        ResponseEntity<ApiResponse<java.util.List<UserDto>>> response = 
            userController.getUserList(null, null, null);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetUserById() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setUsername("admin");
        
        when(userService.getUserById(1L)).thenReturn(user);
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.getUserById(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("admin", response.getBody().getData().getUsername());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userService.getUserById(999L)).thenReturn(null);
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.getUserById(999L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        request.setRoleId(2L);
        
        UserDto created = new UserDto();
        created.setId(3L);
        created.setUsername("newuser");
        
        when(userService.createUser(request)).thenReturn(created);
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.createUser(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testCreateUserUsernameExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("admin");
        
        when(userService.createUser(request))
            .thenThrow(new RuntimeException("用户名已存在"));
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.createUser(request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getCode());
    }

    @Test
    void testUpdateUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("newusername");
        
        UserDto updated = new UserDto();
        updated.setId(1L);
        updated.setUsername("newusername");
        
        when(userService.updateUser(1L, request)).thenReturn(updated);
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.updateUser(1L, request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userService).deleteUser(1L);
        
        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testEnableUser() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setEnabled(true);
        
        when(userService.enableUser(1L)).thenReturn(user);
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.enableUser(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testDisableUser() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setEnabled(false);
        
        when(userService.disableUser(1L)).thenReturn(user);
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.disableUser(1L);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }

    @Test
    void testResetPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newpassword");
        
        UserDto user = new UserDto();
        user.setId(1L);
        
        when(userService.resetPassword(1L, "newpassword")).thenReturn(user);
        
        ResponseEntity<ApiResponse<UserDto>> response = userController.resetPassword(1L, request);
        
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, response.getBody().getCode());
    }
}