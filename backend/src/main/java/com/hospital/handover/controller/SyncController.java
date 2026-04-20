package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.BatchSyncResultDto;
import com.hospital.handover.dto.SyncResultDto;
import com.hospital.handover.service.DeptPatientInfoSyncService;
import com.hospital.handover.service.SyncService;
import com.hospital.handover.service.VitalSignSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    private final SyncService syncService;
    private final VitalSignSyncService vitalSignSyncService;
    private final DeptPatientInfoSyncService deptPatientInfoSyncService;

    public SyncController(SyncService syncService,
                          VitalSignSyncService vitalSignSyncService,
                          DeptPatientInfoSyncService deptPatientInfoSyncService) {
        this.syncService = syncService;
        this.vitalSignSyncService = vitalSignSyncService;
        this.deptPatientInfoSyncService = deptPatientInfoSyncService;
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
    
    @PostMapping("/vital-signs")
    public ResponseEntity<ApiResponse<VitalSignSyncResultDto>> syncVitalSigns(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        logger.info("手动触发生命体征数据同步: startTime={}, endTime={}", startTime, endTime);
        
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(6);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        try {
            VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(startTime, endTime);
            
            VitalSignSyncResultDto dto = new VitalSignSyncResultDto();
            dto.setStatus(result.getStatus());
            dto.setMessage(result.getMessage());
            dto.setTotalCount(result.getTotalCount());
            dto.setSuccessCount(result.getSuccessCount());
            dto.setSkipCount(result.getSkipCount());
            dto.setFailCount(result.getFailCount());
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(ApiResponse.success(dto));
            } else {
                return ResponseEntity.ok(ApiResponse.error(500, result.getMessage(), dto));
            }
        } catch (Exception e) {
            logger.error("手动触发生命体征数据同步失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "同步失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/dept-patient-info")
    public ResponseEntity<ApiResponse<DeptPatientInfoSyncResultDto>> syncDeptPatientInfo(
            @RequestParam(required = false) String deptCode) {
        
        logger.info("手动触发科室患者信息总览数据同步: deptCode={}", deptCode != null ? deptCode : "全部");
        
        try {
            DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo(deptCode);
            
            DeptPatientInfoSyncResultDto dto = new DeptPatientInfoSyncResultDto();
            dto.setStatus(result.getStatus());
            dto.setMessage(result.getMessage());
            dto.setTotalCount(result.getTotalCount());
            dto.setSuccessCount(result.getSuccessCount());
            dto.setSkipCount(result.getSkipCount());
            dto.setFailCount(result.getFailCount());
            
            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(ApiResponse.success(dto));
            } else {
                return ResponseEntity.ok(ApiResponse.error(500, result.getMessage(), dto));
            }
        } catch (Exception e) {
            logger.error("手动触发科室患者信息总览数据同步失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "同步失败: " + e.getMessage()));
        }
    }
    
    public static class VitalSignSyncResultDto {
        private String status;
        private String message;
        private int totalCount;
        private int successCount;
        private int skipCount;
        private int failCount;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getSkipCount() { return skipCount; }
        public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
        
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
    }
    
    public static class DeptPatientInfoSyncResultDto {
        private String status;
        private String message;
        private int totalCount;
        private int successCount;
        private int skipCount;
        private int failCount;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getSkipCount() { return skipCount; }
        public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
        
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
    }
}