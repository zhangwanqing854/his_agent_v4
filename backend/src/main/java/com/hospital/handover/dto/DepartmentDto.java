package com.hospital.handover.dto;

public class DepartmentDto {
    
    private Long id;
    private String code;
    private String name;
    private Integer bedCount;
    
    public DepartmentDto() {}
    
    public DepartmentDto(Long id, String code, String name, Integer bedCount) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.bedCount = bedCount;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getBedCount() { return bedCount; }
    public void setBedCount(Integer bedCount) { this.bedCount = bedCount; }
}