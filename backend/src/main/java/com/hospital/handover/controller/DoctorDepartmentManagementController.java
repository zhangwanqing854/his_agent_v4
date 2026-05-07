package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.DoctorDepartmentManagementDto;
import com.hospital.handover.service.DoctorDepartmentManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/doctor-department-management")
public class DoctorDepartmentManagementController {

    private final DoctorDepartmentManagementService service;

    public DoctorDepartmentManagementController(DoctorDepartmentManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.getList(page, size, search));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorDepartmentManagementDto>> create(
            @RequestParam Long doctorId,
            @RequestParam Long departmentId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPrimary) {
        return ResponseEntity.ok(service.create(doctorId, departmentId, isPrimary));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDepartmentManagementDto>> update(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean isPrimary) {
        return ResponseEntity.ok(service.update(id, isPrimary));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping("/set-primary")
    public ResponseEntity<ApiResponse<Void>> setPrimary(
            @RequestParam Long doctorId,
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(service.setPrimary(doctorId, departmentId));
    }
}