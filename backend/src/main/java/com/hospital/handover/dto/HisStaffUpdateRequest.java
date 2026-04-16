package com.hospital.handover.dto;

public class HisStaffUpdateRequest {
    
    private String name;
    private Long departmentId;
    private String title;
    private String phone;
    
    public HisStaffUpdateRequest() {}
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}