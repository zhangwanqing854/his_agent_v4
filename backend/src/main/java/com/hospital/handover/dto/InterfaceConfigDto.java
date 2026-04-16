package com.hospital.handover.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InterfaceConfigDto {
    
    private Long id;
    private String configCode;
    private String configName;
    private String system;
    private String mode;
    private String protocol;
    private String apiProtocol;
    private String method;
    private String url;
    private String dataType;
    private String syncMode;
    private String syncSchedule;
    private String requestTemplate;
    private String authType;
    private String authConfig;
    private Integer retryInterval;
    private Integer maxRetries;
    private String onFailure;
    private String syncTimeParamStart;
    private String syncTimeParamEnd;
    private String syncTimeFormat;
    private Integer firstSyncDays;
    private Integer incrementalSyncHours;
    private LocalDateTime lastSyncTime;
    private String lastSyncStatus;
    private Integer lastSyncCount;
    private Boolean isFirstSync;
    private Boolean enabled;
    private String description;
    private String soapAction;
    private String soapNamespace;
    private String soapParams;
    private List<MappingTableDto> mappingTables;
    
    public InterfaceConfigDto() {}
    
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
    
    public String getSoapAction() { return soapAction; }
    public void setSoapAction(String soapAction) { this.soapAction = soapAction; }
    
    public String getSoapNamespace() { return soapNamespace; }
    public void setSoapNamespace(String soapNamespace) { this.soapNamespace = soapNamespace; }
    
    public String getSoapParams() { return soapParams; }
    public void setSoapParams(String soapParams) { this.soapParams = soapParams; }

    public List<MappingTableDto> getMappingTables() { return mappingTables; }
    public void setMappingTables(List<MappingTableDto> mappingTables) { this.mappingTables = mappingTables; }
}