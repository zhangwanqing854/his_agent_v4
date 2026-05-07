package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.DutyStaffService;
import com.hospital.handover.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling/duty-staff")
public class DutyStaffController {

    private final DutyStaffService dutyStaffService;
    private final JwtUtil jwtUtil;

    public DutyStaffController(DutyStaffService dutyStaffService, JwtUtil jwtUtil) {
        this.dutyStaffService = dutyStaffService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDutyStaffDto>>> getDutyStaffList(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
        List<DepartmentDutyStaffDto> list = dutyStaffService.getDutyStaffList(departmentId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<DepartmentDutyStaffDto>>> addDutyStaff(
            @RequestBody AddDutyStaffRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        try {
            Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
            List<DepartmentDutyStaffDto> added = dutyStaffService.addDutyStaff(departmentId, request.getStaffIds());
            return ResponseEntity.ok(ApiResponse.success("添加成功", added));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{staffId}")
    public ResponseEntity<ApiResponse<Void>> removeDutyStaff(
            @PathVariable Long staffId,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        try {
            Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
            dutyStaffService.removeDutyStaff(departmentId, staffId);
            return ResponseEntity.ok(ApiResponse.success("移除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/order")
    public ResponseEntity<ApiResponse<List<DepartmentDutyStaffDto>>> updateOrder(
            @RequestBody UpdateDutyStaffOrderRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        try {
            Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
            List<DepartmentDutyStaffDto> updated = dutyStaffService.updateOrder(departmentId, request.getStaffIds());
            return ResponseEntity.ok(ApiResponse.success("排序更新成功", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<List<DepartmentDutyStaffDto>>> initializeDutyStaff(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        try {
            Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
            List<DepartmentDutyStaffDto> initialized = dutyStaffService.initializeDutyStaff(departmentId);
            return ResponseEntity.ok(ApiResponse.success("初始化成功", initialized));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    private Long getCurrentDepartmentId(String authorization, Long headerDeptId) {
        if (headerDeptId != null) {
            return headerDeptId;
        }
        String token = authorization.replace("Bearer ", "");
        return jwtUtil.getDepartmentIdFromToken(token);
    }
}