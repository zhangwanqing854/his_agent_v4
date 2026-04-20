package com.hospital.handover.exception;

public class SyncException extends RuntimeException {
    
    private final String syncType;
    private final int retryCount;
    
    public SyncException(String message) {
        super(message);
        this.syncType = null;
        this.retryCount = 0;
    }
    
    public SyncException(String message, String syncType) {
        super(message);
        this.syncType = syncType;
        this.retryCount = 0;
    }
    
    public SyncException(String message, String syncType, int retryCount) {
        super(message);
        this.syncType = syncType;
        this.retryCount = retryCount;
    }
    
    public SyncException(String message, Throwable cause) {
        super(message, cause);
        this.syncType = null;
        this.retryCount = 0;
    }
    
    public SyncException(String message, String syncType, Throwable cause) {
        super(message, cause);
        this.syncType = syncType;
        this.retryCount = 0;
    }
    
    public String getSyncType() { return syncType; }
    public int getRetryCount() { return retryCount; }
}