package com.hospital.handover.dto;

import java.util.List;

public class BatchSyncResultDto {
    private Boolean success;
    private String message;
    private List<SyncItemResultDto> items;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private Long durationMs;

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public List<SyncItemResultDto> getItems() { return items; }
    public void setItems(List<SyncItemResultDto> items) { this.items = items; }
    
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}