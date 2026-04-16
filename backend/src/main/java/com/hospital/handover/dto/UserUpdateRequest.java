package com.hospital.handover.dto;

public class UserUpdateRequest {
    
    private String username;
    private String password;
    private Long roleId;
    private Long hisStaffId;
    private Boolean enabled;
    
    public UserUpdateRequest() {}
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    
    public Long getHisStaffId() { return hisStaffId; }
    public void setHisStaffId(Long hisStaffId) { this.hisStaffId = hisStaffId; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}