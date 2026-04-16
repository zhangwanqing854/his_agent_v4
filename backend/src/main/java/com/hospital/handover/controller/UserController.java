package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Boolean enabled) {
        
        UserFilterRequest filter = new UserFilterRequest();
        filter.setUsername(username);
        filter.setRoleId(roleId);
        filter.setEnabled(enabled);
        
        List<UserDto> users = userService.getUserList(filter);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody UserCreateRequest request) {
        try {
            UserDto user = userService.createUser(request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        try {
            UserDto user = userService.updateUser(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<UserDto>> enableUser(@PathVariable Long id) {
        try {
            UserDto user = userService.enableUser(id);
            return ResponseEntity.ok(ApiResponse.success("启用成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<UserDto>> disableUser(@PathVariable Long id) {
        try {
            UserDto user = userService.disableUser(id);
            return ResponseEntity.ok(ApiResponse.success("禁用成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<UserDto>> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request) {
        try {
            UserDto user = userService.resetPassword(id, request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", user));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}