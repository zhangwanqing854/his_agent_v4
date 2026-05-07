package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.service.PatientImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patient-import")
public class PatientImportController {

    private final PatientImportService patientImportService;

    public PatientImportController(PatientImportService patientImportService) {
        this.patientImportService = patientImportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportResultDto>> importPatient(
            @RequestParam("file") MultipartFile file) {
        try {
            ImportResultDto result = patientImportService.importFromCsv(file);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}