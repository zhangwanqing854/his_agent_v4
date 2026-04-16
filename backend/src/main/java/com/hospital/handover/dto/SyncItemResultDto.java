package com.hospital.handover.dto;

public class SyncItemResultDto {
    private String configName;
    private Boolean success;
    private String message;
    private Integer totalCount;
    private Integer insertCount;
    private Integer updateCount;
    private Integer skipCount;

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    
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
}