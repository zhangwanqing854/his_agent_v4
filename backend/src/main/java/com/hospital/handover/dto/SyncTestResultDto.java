package com.hospital.handover.dto;

import java.util.List;

public class SyncTestResultDto {
    
    private Boolean success;
    private String message;
    private Long responseTime;
    private Integer dataCount;
    private List<Object> dataPreview;
    private String errorDetail;
    
    public SyncTestResultDto() {}
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Long getResponseTime() { return responseTime; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
    
    public Integer getDataCount() { return dataCount; }
    public void setDataCount(Integer dataCount) { this.dataCount = dataCount; }
    
    public List<Object> getDataPreview() { return dataPreview; }
    public void setDataPreview(List<Object> dataPreview) { this.dataPreview = dataPreview; }
    
    public String getErrorDetail() { return errorDetail; }
    public void setErrorDetail(String errorDetail) { this.errorDetail = errorDetail; }
}