package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.service.VisitImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/visit-import")
public class VisitImportController {

    private final VisitImportService visitImportService;

    public VisitImportController(VisitImportService visitImportService) {
        this.visitImportService = visitImportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportResultDto>> importVisit(
            @RequestParam("file") MultipartFile file) {
        try {
            ImportResultDto result = visitImportService.importFromCsv(file);
            return ResponseEntity.ok(ApiResponse.success("导入完成", result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}