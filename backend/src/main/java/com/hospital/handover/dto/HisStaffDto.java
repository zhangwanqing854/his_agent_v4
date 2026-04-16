package com.hospital.handover.dto;

public class HisStaffDto {
    
    private Long id;
    private String staffCode;
    private String name;
    private Long departmentId;
    private String departmentName;
    private String title;
    private String phone;
    private String syncTime;
    
    public HisStaffDto() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getSyncTime() { return syncTime; }
    public void setSyncTime(String syncTime) { this.syncTime = syncTime; }
}