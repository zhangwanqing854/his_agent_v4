package com.hospital.handover.dto;

import java.util.List;

public class AutoGenerateRequest {
    
    private String startDate;
    private String endDate;
    private Integer startPosition;
    private List<Long> staffOrder;
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public Integer getStartPosition() { return startPosition; }
    public void setStartPosition(Integer startPosition) { this.startPosition = startPosition; }
    
    public List<Long> getStaffOrder() { return staffOrder; }
    public void setStaffOrder(List<Long> staffOrder) { this.staffOrder = staffOrder; }
}