package com.hospital.handover.dto;

public class DepartmentCreateRequest {
    
    private String code;
    private String name;
    private Integer bedCount = 0;
    
    public DepartmentCreateRequest() {}
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getBedCount() { return bedCount; }
    public void setBedCount(Integer bedCount) { this.bedCount = bedCount; }
}