package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.BatchSyncResultDto;
import com.hospital.handover.dto.SyncResultDto;
import com.hospital.handover.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/execute/{configId}")
    public ResponseEntity<ApiResponse<SyncResultDto>> executeSync(
            @PathVariable Long configId,
            @RequestParam(required = false) String deptCode) {
        SyncResultDto result = syncService.executeSync(configId, deptCode);
        
        if (result.getSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
        } else {
            return ResponseEntity.ok(ApiResponse.error(result.getMessage()));
        }
    }
    
    @PostMapping("/execute-batch")
    public ResponseEntity<ApiResponse<BatchSyncResultDto>> executeBatchSync(
            @RequestParam(required = false) String deptCode) {
        BatchSyncResultDto result = syncService.executeBatchSync(deptCode);
        
        if (result.getSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
        } else {
            return ResponseEntity.ok(ApiResponse.error(result.getMessage()));
        }
    }
}