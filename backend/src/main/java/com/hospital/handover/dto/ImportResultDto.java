package com.hospital.handover.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDto {
    
    private int totalCount;
    private int insertCount;
    private int updateCount;
    private int skipCount;
    private int failCount;
    private List<ImportError> errors;
    
    public ImportResultDto() {
        this.errors = new ArrayList<>();
    }
    
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    
    public int getInsertCount() { return insertCount; }
    public void setInsertCount(int insertCount) { this.insertCount = insertCount; }
    
    public int getUpdateCount() { return updateCount; }
    public void setUpdateCount(int updateCount) { this.updateCount = updateCount; }
    
    public int getSkipCount() { return skipCount; }
    public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
    
    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }
    
    public List<ImportError> getErrors() { return errors; }
    public void setErrors(List<ImportError> errors) { this.errors = errors; }
    
    public void addError(int lineNumber, String message) {
        this.errors.add(new ImportError(lineNumber, message));
        this.failCount++;
    }
    
    public static class ImportError {
        private int lineNumber;
        private String message;
        
        public ImportError(int lineNumber, String message) {
            this.lineNumber = lineNumber;
            this.message = message;
        }
        
        public int getLineNumber() { return lineNumber; }
        public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}