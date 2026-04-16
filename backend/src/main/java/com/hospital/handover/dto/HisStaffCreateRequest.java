package com.hospital.handover.dto;

public class HisStaffCreateRequest {
    
    private String staffCode;
    private String name;
    private Long departmentId;
    private String title;
    private String phone;
    
    public HisStaffCreateRequest() {}
    
    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}