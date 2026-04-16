package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interface_config")
public class InterfaceConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_code", nullable = false, unique = true, length = 50)
    private String configCode;
    
    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;
    
    @Column(name = "`system`", nullable = false, length = 50)
    private String system;
    
    @Column(name = "`mode`", nullable = false, length = 20)
    private String mode;
    
    @Column(nullable = false, length = 20)
    private String protocol;
    
    @Column(name = "api_protocol", nullable = false, length = 20)
    private String apiProtocol;
    
    @Column(length = 10)
    private String method;
    
    @Column(nullable = false, length = 500)
    private String url;
    
    @Column(name = "data_type", length = 50)
    private String dataType;
    
    @Column(name = "sync_mode", length = 20)
    private String syncMode = "MANUAL";
    
    @Column(name = "sync_schedule", length = 100)
    private String syncSchedule;
    
    @Column(name = "sync_order")
    private Integer syncOrder;
    
    @Column(name = "request_template", columnDefinition = "TEXT")
    private String requestTemplate;
    
    @Column(name = "auth_type", length = 20)
    private String authType = "NONE";
    
    @Column(name = "auth_config", columnDefinition = "JSON")
    private String authConfig;
    
    @Column(name = "retry_interval")
    private Integer retryInterval = 5;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "on_failure", length = 20)
    private String onFailure = "ALERT";
    
    @Column(name = "sync_time_param_start", length = 50)
    private String syncTimeParamStart = "startTime";
    
    @Column(name = "sync_time_param_end", length = 50)
    private String syncTimeParamEnd = "endTime";
    
    @Column(name = "dept_param", length = 20)
    private String deptParam;
    
    @Column(name = "sync_time_format", length = 20)
    private String syncTimeFormat = "yyyy-MM-dd HH:mm:ss";
    
    @Column(name = "first_sync_days")
    private Integer firstSyncDays = 30;
    
    @Column(name = "incremental_sync_hours")
    private Integer incrementalSyncHours = 24;
    
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
    
    @Column(name = "last_sync_status", length = 20)
    private String lastSyncStatus;
    
    @Column(name = "last_sync_count")
    private Integer lastSyncCount;
    
    @Column(name = "is_first_sync")
    private Boolean isFirstSync = true;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "soap_action", length = 100)
    private String soapAction;
    
    @Column(name = "soap_namespace", length = 200)
    private String soapNamespace;
    
    @Column(name = "soap_params", columnDefinition = "JSON")
    private String soapParams;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getConfigCode() { return configCode; }
    public void setConfigCode(String configCode) { this.configCode = configCode; }
    
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    
    public String getSystem() { return system; }
    public void setSystem(String system) { this.system = system; }
    
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    
    public String getApiProtocol() { return apiProtocol; }
    public void setApiProtocol(String apiProtocol) { this.apiProtocol = apiProtocol; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    
    public String getSyncMode() { return syncMode; }
    public void setSyncMode(String syncMode) { this.syncMode = syncMode; }
    
    public String getSyncSchedule() { return syncSchedule; }
    public void setSyncSchedule(String syncSchedule) { this.syncSchedule = syncSchedule; }
    
    public Integer getSyncOrder() { return syncOrder; }
    public void setSyncOrder(Integer syncOrder) { this.syncOrder = syncOrder; }
    
    public String getRequestTemplate() { return requestTemplate; }
    public void setRequestTemplate(String requestTemplate) { this.requestTemplate = requestTemplate; }
    
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    
    public String getAuthConfig() { return authConfig; }
    public void setAuthConfig(String authConfig) { this.authConfig = authConfig; }
    
    public Integer getRetryInterval() { return retryInterval; }
    public void setRetryInterval(Integer retryInterval) { this.retryInterval = retryInterval; }
    
    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    
    public String getOnFailure() { return onFailure; }
    public void setOnFailure(String onFailure) { this.onFailure = onFailure; }
    
    public String getSyncTimeParamStart() { return syncTimeParamStart; }
    public void setSyncTimeParamStart(String syncTimeParamStart) { this.syncTimeParamStart = syncTimeParamStart; }
    
    public String getSyncTimeParamEnd() { return syncTimeParamEnd; }
    public void setSyncTimeParamEnd(String syncTimeParamEnd) { this.syncTimeParamEnd = syncTimeParamEnd; }
    
    public String getDeptParam() { return deptParam; }
    public void setDeptParam(String deptParam) { this.deptParam = deptParam; }
    
    public String getSyncTimeFormat() { return syncTimeFormat; }
    public void setSyncTimeFormat(String syncTimeFormat) { this.syncTimeFormat = syncTimeFormat; }
    
    public Integer getFirstSyncDays() { return firstSyncDays; }
    public void setFirstSyncDays(Integer firstSyncDays) { this.firstSyncDays = firstSyncDays; }
    
    public Integer getIncrementalSyncHours() { return incrementalSyncHours; }
    public void setIncrementalSyncHours(Integer incrementalSyncHours) { this.incrementalSyncHours = incrementalSyncHours; }
    
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    
    public String getLastSyncStatus() { return lastSyncStatus; }
    public void setLastSyncStatus(String lastSyncStatus) { this.lastSyncStatus = lastSyncStatus; }
    
    public Integer getLastSyncCount() { return lastSyncCount; }
    public void setLastSyncCount(Integer lastSyncCount) { this.lastSyncCount = lastSyncCount; }
    
    public Boolean getIsFirstSync() { return isFirstSync; }
    public void setIsFirstSync(Boolean isFirstSync) { this.isFirstSync = isFirstSync; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getSoapAction() { return soapAction; }
    public void setSoapAction(String soapAction) { this.soapAction = soapAction; }
    
    public String getSoapNamespace() { return soapNamespace; }
    public void setSoapNamespace(String soapNamespace) { this.soapNamespace = soapNamespace; }
    
    public String getSoapParams() { return soapParams; }
    public void setSoapParams(String soapParams) { this.soapParams = soapParams; }
}