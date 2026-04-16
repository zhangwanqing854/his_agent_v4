package com.hospital.handover.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HandoverDto {
    
    private Long id;
    private Long deptId;
    private String deptName;
    private String handoverNo;
    private LocalDate handoverDate;
    private String shift;
    private Long fromDoctorId;
    private String fromDoctorName;
    private Long toDoctorId;
    private String toDoctorName;
    private String status;
    private Integer patientCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public HandoverDto() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    
    public String getHandoverNo() { return handoverNo; }
    public void setHandoverNo(String handoverNo) { this.handoverNo = handoverNo; }
    
    public LocalDate getHandoverDate() { return handoverDate; }
    public void setHandoverDate(LocalDate handoverDate) { this.handoverDate = handoverDate; }
    
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    
    public Long getFromDoctorId() { return fromDoctorId; }
    public void setFromDoctorId(Long fromDoctorId) { this.fromDoctorId = fromDoctorId; }
    
    public String getFromDoctorName() { return fromDoctorName; }
    public void setFromDoctorName(String fromDoctorName) { this.fromDoctorName = fromDoctorName; }
    
    public Long getToDoctorId() { return toDoctorId; }
    public void setToDoctorId(Long toDoctorId) { this.toDoctorId = toDoctorId; }
    
    public String getToDoctorName() { return toDoctorName; }
    public void setToDoctorName(String toDoctorName) { this.toDoctorName = toDoctorName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getPatientCount() { return patientCount; }
    public void setPatientCount(Integer patientCount) { this.patientCount = patientCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
