package com.hospital.handover.dto;

import java.util.List;

public class RoleDto {
    
    private Long id;
    private String code;
    private String name;
    private Boolean isDefault;
    private String description;
    private List<Long> dutyIds;
    
    public RoleDto() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<Long> getDutyIds() { return dutyIds; }
    public void setDutyIds(List<Long> dutyIds) { this.dutyIds = dutyIds; }
}