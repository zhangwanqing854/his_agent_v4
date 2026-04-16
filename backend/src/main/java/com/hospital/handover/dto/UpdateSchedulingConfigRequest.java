package com.hospital.handover.dto;

import java.util.List;

public class UpdateSchedulingConfigRequest {
    
    private List<Long> staffOrder;
    
    public List<Long> getStaffOrder() { return staffOrder; }
    public void setStaffOrder(List<Long> staffOrder) { this.staffOrder = staffOrder; }
}