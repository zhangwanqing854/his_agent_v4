package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Role;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.RoleRepository;
import com.hospital.handover.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        userService = new UserService(userRepository, roleRepository);
    }

    @Test
    void testGetUserList() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setRoleId(1L);
        user1.setEnabled(true);
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("doctor1");
        user2.setRoleId(2L);
        user2.setEnabled(true);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        Role role = new Role();
        role.setName("医生");
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(role));
        
        var result = userService.getUserList(null);
        
        assertEquals(2, result.size());
    }

    @Test
    void testGetUserListWithFilter() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setRoleId(1L);
        user1.setEnabled(true);
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("doctor1");
        user2.setRoleId(2L);
        user2.setEnabled(false);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        UserFilterRequest filter = new UserFilterRequest();
        filter.setEnabled(true);
        
        var result = userService.getUserList(filter);
        
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setRoleId(1L);
        user.setEnabled(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Role role = new Role();
        role.setName("管理员");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        
        UserDto result = userService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        UserDto result = userService.getUserById(999L);
        
        assertNull(result);
    }

    @Test
    void testCreateUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRoleId(2L);
        
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        
        User saved = new User();
        saved.setId(3L);
        saved.setUsername("newuser");
        saved.setRoleId(2L);
        saved.setEnabled(true);
        
        when(userRepository.save(any(User.class))).thenReturn(saved);
        
        Role role = new Role();
        role.setName("医生");
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        
        UserDto result = userService.createUser(request);
        
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
    }

    @Test
    void testCreateUserUsernameExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("admin");
        request.setPassword("password");
        request.setRoleId(1L);
        
        when(userRepository.findByUsername("admin")).thenReturn(new User());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(request);
        });
        
        assertTrue(exception.getMessage().contains("用户名已存在"));
    }

    @Test
    void testUpdateUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("newusername");
        
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("admin");
        existing.setRoleId(1L);
        existing.setEnabled(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("newusername")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        
        Role role = new Role();
        role.setName("管理员");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        
        UserDto result = userService.updateUser(1L, request);
        
        assertNotNull(result);
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setId(1L);
        user.setIsSuperAdmin(false);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteSuperAdmin() {
        User user = new User();
        user.setId(1L);
        user.setIsSuperAdmin(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(1L);
        });
        
        assertTrue(exception.getMessage().contains("无法删除超级管理员"));
    }

    @Test
    void testEnableUser() {
        User user = new User();
        user.setId(1L);
        user.setEnabled(false);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        
        UserDto result = userService.enableUser(1L);
        
        assertNotNull(result);
    }

    @Test
    void testDisableUser() {
        User user = new User();
        user.setId(1L);
        user.setIsSuperAdmin(false);
        user.setEnabled(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        
        UserDto result = userService.disableUser(1L);
        
        assertNotNull(result);
    }

    @Test
    void testDisableSuperAdmin() {
        User user = new User();
        user.setId(1L);
        user.setIsSuperAdmin(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.disableUser(1L);
        });
        
        assertTrue(exception.getMessage().contains("无法禁用超级管理员"));
    }

    @Test
    void testResetPassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("oldpassword");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        
        UserDto result = userService.resetPassword(1L, "newpassword");
        
        assertNotNull(result);
    }
}