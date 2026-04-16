package com.hospital.handover.dto;

import java.util.List;

public class UpdateDutyStaffOrderRequest {
    
    private List<Long> staffIds;
    
    public List<Long> getStaffIds() { return staffIds; }
    public void setStaffIds(List<Long> staffIds) { this.staffIds = staffIds; }
}