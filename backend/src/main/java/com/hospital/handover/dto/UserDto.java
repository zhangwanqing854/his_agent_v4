package com.hospital.handover.dto;

public class UserDto {
    
    private Long id;
    private String username;
    private String usercode;
    private String name;
    private Boolean isSuperAdmin;
    private Long hisStaffId;
    private Long roleId;
    private String roleName;
    private Boolean enabled;
    
    public UserDto() {}
    
    public UserDto(Long id, String username, Boolean enabled) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getUsercode() { return usercode; }
    public void setUsercode(String usercode) { this.usercode = usercode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Boolean getIsSuperAdmin() { return isSuperAdmin; }
    public void setIsSuperAdmin(Boolean isSuperAdmin) { this.isSuperAdmin = isSuperAdmin; }
    
    public Long getHisStaffId() { return hisStaffId; }
    public void setHisStaffId(Long hisStaffId) { this.hisStaffId = hisStaffId; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}