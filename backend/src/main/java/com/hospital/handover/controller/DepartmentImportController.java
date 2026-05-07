package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.service.DepartmentImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/department")
public class DepartmentImportController {

    private final DepartmentImportService departmentImportService;

    public DepartmentImportController(DepartmentImportService departmentImportService) {
        this.departmentImportService = departmentImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<ImportResultDto>> importDepartments(
            @RequestParam("file") MultipartFile file) {
        try {
            ImportResultDto result = departmentImportService.importFromCsv(file);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}