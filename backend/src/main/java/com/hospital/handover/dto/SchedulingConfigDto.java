package com.hospital.handover.dto;

import java.util.List;

public class SchedulingConfigDto {
    
    private Long departmentId;
    private List<Long> staffOrder;
    private Integer lastPosition;
    private String updatedAt;
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public List<Long> getStaffOrder() { return staffOrder; }
    public void setStaffOrder(List<Long> staffOrder) { this.staffOrder = staffOrder; }
    
    public Integer getLastPosition() { return lastPosition; }
    public void setLastPosition(Integer lastPosition) { this.lastPosition = lastPosition; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}