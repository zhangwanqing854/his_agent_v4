package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_record")
public class SyncRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_id", nullable = false)
    private Long configId;
    
    @Column(name = "config_code", nullable = false, length = 50)
    private String configCode;
    
    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;
    
    @Column(name = "sync_type", nullable = false, length = 20)
    private String syncType;
    
    @Column(name = "sync_start_time", nullable = false)
    private LocalDateTime syncStartTime;
    
    @Column(name = "sync_end_time", nullable = false)
    private LocalDateTime syncEndTime;
    
    @Column(name = "actual_start_time", nullable = false)
    private LocalDateTime actualStartTime;
    
    @Column(name = "actual_end_time", nullable = false)
    private LocalDateTime actualEndTime;
    
    @Column(name = "sync_status", nullable = false, length = 20)
    private String syncStatus;
    
    @Column(name = "record_count")
    private Integer recordCount;
    
    @Column(name = "success_count")
    private Integer successCount;
    
    @Column(name = "fail_count")
    private Integer failCount;
    
    @Column(name = "skip_count")
    private Integer skipCount;
    
    @Column(name = "update_count")
    private Integer updateCount;
    
    @Column(name = "insert_count")
    private Integer insertCount;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;
    
    @Column(name = "response_sample", columnDefinition = "TEXT")
    private String responseSample;
    
    @Column(name = "duration_ms")
    private Integer durationMs;
    
    @Column(name = "operator_id")
    private Long operatorId;
    
    @Column(name = "operator_name", length = 50)
    private String operatorName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    
    public String getConfigCode() { return configCode; }
    public void setConfigCode(String configCode) { this.configCode = configCode; }
    
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    
    public String getSyncType() { return syncType; }
    public void setSyncType(String syncType) { this.syncType = syncType; }
    
    public LocalDateTime getSyncStartTime() { return syncStartTime; }
    public void setSyncStartTime(LocalDateTime syncStartTime) { this.syncStartTime = syncStartTime; }
    
    public LocalDateTime getSyncEndTime() { return syncEndTime; }
    public void setSyncEndTime(LocalDateTime syncEndTime) { this.syncEndTime = syncEndTime; }
    
    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }
    
    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }
    
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    
    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }
    
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    
    public Integer getFailCount() { return failCount; }
    public void setFailCount(Integer failCount) { this.failCount = failCount; }
    
    public Integer getSkipCount() { return skipCount; }
    public void setSkipCount(Integer skipCount) { this.skipCount = skipCount; }
    
    public Integer getUpdateCount() { return updateCount; }
    public void setUpdateCount(Integer updateCount) { this.updateCount = updateCount; }
    
    public Integer getInsertCount() { return insertCount; }
    public void setInsertCount(Integer insertCount) { this.insertCount = insertCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getRequestData() { return requestData; }
    public void setRequestData(String requestData) { this.requestData = requestData; }
    
    public String getResponseSample() { return responseSample; }
    public void setResponseSample(String responseSample) { this.responseSample = responseSample; }
    
    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
    
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}