package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.service.DoctorDepartmentImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/doctor-department-import")
public class DoctorDepartmentImportController {

    private final DoctorDepartmentImportService doctorDepartmentImportService;

    public DoctorDepartmentImportController(DoctorDepartmentImportService doctorDepartmentImportService) {
        this.doctorDepartmentImportService = doctorDepartmentImportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportResultDto>> importDoctorDepartment(
            @RequestParam("file") MultipartFile file) {
        try {
            ImportResultDto result = doctorDepartmentImportService.importFromCsv(file);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}