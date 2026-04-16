package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionDto>>> getAllPermissions() {
        List<PermissionDto> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionDto>> getPermissionById(@PathVariable Long id) {
        PermissionDto perm = permissionService.getPermissionById(id);
        if (perm == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "权限不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(perm));
    }

    @GetMapping("/{id}/duties")
    public ResponseEntity<ApiResponse<List<DutyDto>>> getDutiesByPermission(@PathVariable Long id) {
        List<DutyDto> duties = permissionService.getDutiesByPermissionId(id);
        return ResponseEntity.ok(ApiResponse.success(duties));
    }
}