package com.hospital.handover.dto;

import java.time.LocalDateTime;

public class InpatientDto {
    
    private Long id;
    private String bedNo;
    private String patientNo;
    private String patientName;
    private String nurseLevel;
    private Boolean isCritical;
    private LocalDateTime admissionDatetime;
    private String patientStatus;
    private String deptName;
    
    public InpatientDto() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBedNo() { return bedNo; }
    public void setBedNo(String bedNo) { this.bedNo = bedNo; }
    
    public String getPatientNo() { return patientNo; }
    public void setPatientNo(String patientNo) { this.patientNo = patientNo; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getNurseLevel() { return nurseLevel; }
    public void setNurseLevel(String nurseLevel) { this.nurseLevel = nurseLevel; }
    
    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }
    
    public LocalDateTime getAdmissionDatetime() { return admissionDatetime; }
    public void setAdmissionDatetime(LocalDateTime admissionDatetime) { this.admissionDatetime = admissionDatetime; }
    
    public String getPatientStatus() { return patientStatus; }
    public void setPatientStatus(String patientStatus) { this.patientStatus = patientStatus; }
    
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
}