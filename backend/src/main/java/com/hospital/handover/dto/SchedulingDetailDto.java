package com.hospital.handover.dto;

import java.time.LocalDate;

public class SchedulingDetailDto {
    
    private Long id;
    private String dutyDate;
    private Long staffId;
    private String staffName;
    private String remark;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDutyDate() { return dutyDate; }
    public void setDutyDate(String dutyDate) { this.dutyDate = dutyDate; }
    
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}