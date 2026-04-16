package com.hospital.handover.dto;

public class SyncResultDto {
    private Boolean success;
    private String message;
    private Integer totalCount;
    private Integer insertCount;
    private Integer updateCount;
    private Integer skipCount;
    private Integer failCount;
    private Long durationMs;
    private String errorMessage;

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    
    public Integer getInsertCount() { return insertCount; }
    public void setInsertCount(Integer insertCount) { this.insertCount = insertCount; }
    
    public Integer getUpdateCount() { return updateCount; }
    public void setUpdateCount(Integer updateCount) { this.updateCount = updateCount; }
    
    public Integer getSkipCount() { return skipCount; }
    public void setSkipCount(Integer skipCount) { this.skipCount = skipCount; }
    
    public Integer getFailCount() { return failCount; }
    public void setFailCount(Integer failCount) { this.failCount = failCount; }
    
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}