package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.HisStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/his-staff")
public class HisStaffController {

    private final HisStaffService hisStaffService;

    public HisStaffController(HisStaffService hisStaffService) {
        this.hisStaffService = hisStaffService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HisStaffDto>>> getAllStaff(
            @RequestParam(required = false) Long departmentId) {
        List<HisStaffDto> staff;
        if (departmentId != null) {
            staff = hisStaffService.getStaffByDepartment(departmentId);
        } else {
            staff = hisStaffService.getAllStaff();
        }
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HisStaffDto>> getStaffById(@PathVariable Long id) {
        HisStaffDto staff = hisStaffService.getStaffById(id);
        if (staff == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "员工不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    @GetMapping("/unlinked")
    public ResponseEntity<ApiResponse<List<HisStaffDto>>> getUnlinkedStaff() {
        List<HisStaffDto> staff = hisStaffService.getUnlinkedStaff();
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HisStaffDto>> createStaff(@RequestBody HisStaffCreateRequest request) {
        try {
            HisStaffDto staff = hisStaffService.createStaff(request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", staff));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HisStaffDto>> updateStaff(
            @PathVariable Long id,
            @RequestBody HisStaffUpdateRequest request) {
        try {
            HisStaffDto staff = hisStaffService.updateStaff(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", staff));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        try {
            hisStaffService.deleteStaff(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}