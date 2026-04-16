package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.UserRepository;
import com.hospital.handover.service.HandoverService;
import com.hospital.handover.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/handovers")
public class HandoverController {

    private final HandoverService handoverService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public HandoverController(HandoverService handoverService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.handoverService = handoverService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    private Long getCurrentDoctorId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getHisStaffId() : null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HandoverDto>>> getHandoverList(
            @RequestParam Long deptId) {
        List<HandoverDto> handovers = handoverService.getHandoverList(deptId);
        return ResponseEntity.ok(ApiResponse.success(handovers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HandoverDto>> getHandoverById(@PathVariable Long id) {
        HandoverDto handover = handoverService.getHandoverById(id);
        if (handover == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "交班记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(handover));
    }

    @GetMapping("/{id}/patients")
    public ResponseEntity<ApiResponse<List<HandoverPatientDto>>> getHandoverPatients(
            @PathVariable Long id) {
        List<HandoverPatientDto> patients = handoverService.getHandoverPatients(id);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HandoverDto>> createHandover(
            @RequestBody HandoverCreateRequest request) {
        HandoverDto handover = handoverService.createHandover(request);
        if (handover == null) {
            return ResponseEntity.ok(ApiResponse.error(400, "科室不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(handover));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<HandoverStatsDto>> getStats(@RequestParam Long deptId) {
        HandoverStatsDto stats = handoverService.calculateStats(deptId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/duty-staff")
    public ResponseEntity<ApiResponse<DutyStaffDto>> getDutyStaff(@RequestParam Long deptId) {
        DutyStaffDto dutyStaff = handoverService.getDutyStaff(deptId);
        if (dutyStaff == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "无排班数据，请手动选择接班医生"));
        }
        return ResponseEntity.ok(ApiResponse.success(dutyStaff));
    }

    @GetMapping("/handover-patients")
    public ResponseEntity<ApiResponse<List<HandoverPatientDto>>> getHandoverPatientsForCreate(@RequestParam Long deptId) {
        List<HandoverPatientDto> patients = handoverService.getHandoverPatientsForCreate(deptId);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHandover(@PathVariable Long id) {
        boolean deleted = handoverService.deleteHandover(id);
        if (!deleted) {
            return ResponseEntity.ok(ApiResponse.error(404, "交班记录不存在或无法删除"));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HandoverDto>> updateHandover(
            @PathVariable Long id,
            @RequestBody HandoverCreateRequest request) {
        HandoverDto handover = handoverService.updateHandover(id, request);
        if (handover == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "交班记录不存在或无法修改"));
        }
        return ResponseEntity.ok(ApiResponse.success(handover));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<HandoverDto>> submitHandover(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentDoctorId = getCurrentDoctorId(authHeader);
        if (currentDoctorId == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "未授权"));
        }
        HandoverDto handover = handoverService.submitHandover(id, currentDoctorId);
        if (handover == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有交班医生可以提交，且需选择接班医生"));
        }
        return ResponseEntity.ok(ApiResponse.success(handover));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<HandoverDto>> acceptHandover(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentDoctorId = getCurrentDoctorId(authHeader);
        if (currentDoctorId == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "未授权"));
        }
        HandoverDto handover = handoverService.acceptHandover(id, currentDoctorId);
        if (handover == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有接班医生可以接班"));
        }
        return ResponseEntity.ok(ApiResponse.success(handover));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<HandoverDto>> rejectHandover(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentDoctorId = getCurrentDoctorId(authHeader);
        if (currentDoctorId == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "未授权"));
        }
        HandoverDto handover = handoverService.rejectHandover(id, request.getReason(), currentDoctorId);
        if (handover == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有接班医生可以退回"));
        }
        return ResponseEntity.ok(ApiResponse.success(handover));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<HandoverDto>> confirmHandover(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentDoctorId = getCurrentDoctorId(authHeader);
        if (currentDoctorId == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "未授权"));
        }
        HandoverDto handover = handoverService.acceptHandover(id, currentDoctorId);
        if (handover == null) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有接班医生可以确认完成"));
        }
        return ResponseEntity.ok(ApiResponse.success(handover));
    }

    @GetMapping("/{id}/todos")
    public ResponseEntity<ApiResponse<List<TodoItemDto>>> getHandoverTodos(@PathVariable Long id) {
        List<TodoItemDto> todos = handoverService.getHandoverTodos(id);
        return ResponseEntity.ok(ApiResponse.success(todos));
    }

    @PostMapping("/{id}/todos")
    public ResponseEntity<ApiResponse<TodoItemDto>> createHandoverTodo(
            @PathVariable Long id,
            @RequestBody TodoCreateRequest request) {
        TodoItemDto todo = handoverService.createHandoverTodo(id, request.getContent(), request.getDueTime());
        if (todo == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "交班记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(todo));
    }

    @PutMapping("/todos/{todoId}/complete")
    public ResponseEntity<ApiResponse<TodoItemDto>> completeTodo(@PathVariable Long todoId) {
        TodoItemDto todo = handoverService.completeTodo(todoId);
        if (todo == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "待办事项不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(todo));
    }

    @PutMapping("/{handoverId}/patients/{patientId}")
    public ResponseEntity<ApiResponse<HandoverPatientDto>> updateHandoverPatient(
            @PathVariable Long handoverId,
            @PathVariable Long patientId,
            @RequestBody HandoverPatientUpdateRequest request) {
        HandoverPatientDto patient = handoverService.updateHandoverPatient(handoverId, patientId, request);
        if (patient == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "患者记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(patient));
    }
}