package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.SchedulingService;
import com.hospital.handover.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

    private final SchedulingService schedulingService;
    private final JwtUtil jwtUtil;

    public SchedulingController(SchedulingService schedulingService, JwtUtil jwtUtil) {
        this.schedulingService = schedulingService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchedulingListItemDto>>> getSchedulingList(
            @RequestParam(required = false) String yearMonth,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        
        Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
        List<SchedulingListItemDto> list = schedulingService.getSchedulingList(departmentId, yearMonth);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchedulingDto>> getSchedulingById(@PathVariable Long id) {
        SchedulingDto scheduling = schedulingService.getSchedulingById(id);
        if (scheduling == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "排班不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(scheduling));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchedulingListItemDto>> createScheduling(
            @RequestBody CreateSchedulingRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        try {
            Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
            if (departmentId == null) {
                return ResponseEntity.ok(ApiResponse.error("当前科室为空，请先选择科室"));
            }
            Long userId = getCurrentUserId(authorization);
            
            SchedulingListItemDto scheduling = schedulingService.createScheduling(departmentId, userId, request);
            return ResponseEntity.ok(ApiResponse.success("创建成功", scheduling));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchedulingListItemDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        try {
            SchedulingListItemDto scheduling = schedulingService.updateStatus(id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("更新成功", scheduling));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScheduling(@PathVariable Long id) {
        try {
            schedulingService.deleteScheduling(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/details")
    public ResponseEntity<ApiResponse<List<SchedulingDetailDto>>> updateDetails(
            @PathVariable Long id,
            @RequestBody UpdateSchedulingDetailsRequest request) {
        try {
            List<SchedulingDetailDto> details = schedulingService.updateDetails(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", details));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<HisStaffDto>>> getSchedulableStaff(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
        List<HisStaffDto> staff = schedulingService.getSchedulableStaff(departmentId);
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<SchedulingConfigDto>> getConfig(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
        SchedulingConfigDto config = schedulingService.getConfig(departmentId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<SchedulingConfigDto>> updateConfig(
            @RequestBody UpdateSchedulingConfigRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        try {
            Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
            SchedulingConfigDto config = schedulingService.updateConfig(departmentId, request);
            return ResponseEntity.ok(ApiResponse.success("更新成功", config));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/auto-generate")
    public ResponseEntity<ApiResponse<List<SchedulingDetailDto>>> autoGenerate(
            @PathVariable Long id,
            @RequestBody AutoGenerateRequest request,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Current-Department-Id", required = false) Long headerDeptId) {
        try {
            Long departmentId = getCurrentDepartmentId(authorization, headerDeptId);
            List<SchedulingDetailDto> details = schedulingService.autoGenerate(id, departmentId, request);
            return ResponseEntity.ok(ApiResponse.success("生成成功", details));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    private Long getCurrentUserId(String authorization) {
        String token = authorization.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    private Long getCurrentDepartmentId(String authorization, Long headerDeptId) {
        if (headerDeptId != null) {
            return headerDeptId;
        }
        String token = authorization.replace("Bearer ", "");
        return jwtUtil.getDepartmentIdFromToken(token);
    }
}