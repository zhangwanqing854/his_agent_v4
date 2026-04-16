package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(@PathVariable Long id) {
        RoleDto role = roleService.getRoleById(id);
        if (role == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "角色不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody RoleCreateRequest request) {
        try {
            RoleDto role = roleService.createRole(request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", role));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {
        try {
            RoleDto role = roleService.updateRole(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", role));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/duties")
    public ResponseEntity<ApiResponse<List<DutyDto>>> getDutiesByRole(@PathVariable Long id) {
        List<DutyDto> duties = roleService.getDutiesByRoleId(id);
        return ResponseEntity.ok(ApiResponse.success(duties));
    }
}