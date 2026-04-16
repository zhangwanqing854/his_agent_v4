package com.hospital.handover.dto;

public class DepartmentInfo {
    
    private Long id;
    private String code;
    private String name;
    private Boolean isPrimary;
    
    public DepartmentInfo() {}
    
    public DepartmentInfo(Long id, String code, String name, Boolean isPrimary) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.isPrimary = isPrimary;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
}