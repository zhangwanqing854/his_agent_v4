package com.hospital.handover.dto;

import java.util.List;

public class UpdateSchedulingDetailsRequest {
    
    private List<SchedulingDetailItem> details;
    
    public List<SchedulingDetailItem> getDetails() { return details; }
    public void setDetails(List<SchedulingDetailItem> details) { this.details = details; }
    
    public static class SchedulingDetailItem {
        private String dutyDate;
        private Long staffId;
        private String remark;
        
        public String getDutyDate() { return dutyDate; }
        public void setDutyDate(String dutyDate) { this.dutyDate = dutyDate; }
        
        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}