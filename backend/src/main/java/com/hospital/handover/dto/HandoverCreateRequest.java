package com.hospital.handover.dto;

import java.time.LocalDate;

public class HandoverCreateRequest {
    
    private Long deptId;
    private LocalDate handoverDate;
    private String shift;
    private Long fromDoctorId;
    private Long toDoctorId;
    
    public HandoverCreateRequest() {}
    
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    
    public LocalDate getHandoverDate() { return handoverDate; }
    public void setHandoverDate(LocalDate handoverDate) { this.handoverDate = handoverDate; }
    
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    
    public Long getFromDoctorId() { return fromDoctorId; }
    public void setFromDoctorId(Long fromDoctorId) { this.fromDoctorId = fromDoctorId; }
    
    public Long getToDoctorId() { return toDoctorId; }
    public void setToDoctorId(Long toDoctorId) { this.toDoctorId = toDoctorId; }
}
