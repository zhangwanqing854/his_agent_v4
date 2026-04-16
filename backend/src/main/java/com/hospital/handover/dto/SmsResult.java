package com.hospital.handover.dto;

public class SmsResult {
    
    private boolean success;
    private String errorMessage;
    
    public SmsResult() {}
    
    public static SmsResult success() {
        SmsResult result = new SmsResult();
        result.setSuccess(true);
        return result;
    }
    
    public static SmsResult failure(String errorMessage) {
        SmsResult result = new SmsResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}