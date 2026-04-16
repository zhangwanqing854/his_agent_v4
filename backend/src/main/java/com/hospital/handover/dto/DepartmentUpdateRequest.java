package com.hospital.handover.dto;

public class DepartmentUpdateRequest {
    
    private String name;
    private Integer bedCount;
    
    public DepartmentUpdateRequest() {}
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getBedCount() { return bedCount; }
    public void setBedCount(Integer bedCount) { this.bedCount = bedCount; }
}