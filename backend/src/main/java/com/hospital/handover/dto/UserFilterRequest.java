package com.hospital.handover.dto;

public class UserFilterRequest {
    
    private String username;
    private Long roleId;
    private Boolean enabled;
    
    public UserFilterRequest() {}
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}