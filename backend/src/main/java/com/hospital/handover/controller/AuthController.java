package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.AuthService;
import com.hospital.handover.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("退出成功", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.error(401, "未授权"));
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.ok(ApiResponse.error(401, "Token无效或已过期"));
        }
        
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserInfo userInfo = authService.getCurrentUser(userId);
            return ResponseEntity.ok(ApiResponse.success(userInfo));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(401, e.getMessage()));
        }
    }
}