package com.hospital.handover.dto;

import java.util.List;

public class UserInfo {
    
    private Long id;
    private Long hisStaffId;
    private String username;
    private String name;
    private String role;
    private String avatar;
    private List<DepartmentInfo> departments;
    
    public UserInfo() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getHisStaffId() { return hisStaffId; }
    public void setHisStaffId(Long hisStaffId) { this.hisStaffId = hisStaffId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public List<DepartmentInfo> getDepartments() { return departments; }
    public void setDepartments(List<DepartmentInfo> departments) { this.departments = departments; }
}