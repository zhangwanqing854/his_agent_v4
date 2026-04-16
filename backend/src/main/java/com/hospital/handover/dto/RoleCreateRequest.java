package com.hospital.handover.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class RoleCreateRequest {
    
    @NotBlank(message = "角色编码不能为空")
    private String code;
    
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    private Boolean isDefault = false;
    
    private String description;
    
    private List<Long> dutyIds;
    
    public RoleCreateRequest() {}
    
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