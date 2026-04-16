package com.hospital.handover.dto;

import java.time.LocalDateTime;

public class VisitDto {
    
    private Long id;
    private String visitNo;
    private Long patientId;
    private String patientName;
    private Long departmentId;
    private String departmentName;
    private String bedNo;
    private String diagnosis;
    private String nursingLevel;
    private String visitStatus;
    private LocalDateTime admitTime;
    private LocalDateTime dischargeTime;
    
    public VisitDto() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getVisitNo() { return visitNo; }
    public void setVisitNo(String visitNo) { this.visitNo = visitNo; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public String getBedNo() { return bedNo; }
    public void setBedNo(String bedNo) { this.bedNo = bedNo; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    
    public String getNursingLevel() { return nursingLevel; }
    public void setNursingLevel(String nursingLevel) { this.nursingLevel = nursingLevel; }
    
    public String getVisitStatus() { return visitStatus; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }
    
    public LocalDateTime getAdmitTime() { return admitTime; }
    public void setAdmitTime(LocalDateTime admitTime) { this.admitTime = admitTime; }
    
    public LocalDateTime getDischargeTime() { return dischargeTime; }
    public void setDischargeTime(LocalDateTime dischargeTime) { this.dischargeTime = dischargeTime; }
}