package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.service.HisStaffImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/his-staff-import")
public class HisStaffImportController {

    private final HisStaffImportService hisStaffImportService;

    public HisStaffImportController(HisStaffImportService hisStaffImportService) {
        this.hisStaffImportService = hisStaffImportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportResultDto>> importHisStaff(
            @RequestParam("file") MultipartFile file) {
        try {
            ImportResultDto result = hisStaffImportService.importFromCsv(file);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}