package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit")
public class Visit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "visit_no", nullable = false, unique = true, length = 50)
    private String visitNo;
    
    @Column(name = "patient_id")
    private Long patientId;
    
    @Column(name = "patient_no", length = 50)
    private String patientNo;
    
    @Column(name = "patient_name", length = 50)
    private String patientName;
    
    @Column(name = "patient_his_id", length = 50)
    private String patientHisId;
    
    @Column(name = "dept_id")
    private Long departmentId;
    
    @Column(name = "dept_his_id", length = 50)
    private String deptHisId;
    
    @Column(name = "dept_name", length = 50)
    private String deptName;
    
    @Column(name = "bed_no", length = 20)
    private String bedNo;
    
    @Column(name = "visit_type", length = 20)
    private String visitType;
    
    @Column(name = "admission_count")
    private Integer admissionCount;
    
    @Column(name = "nurse_area_id", length = 50)
    private String nurseAreaId;
    
    @Column(name = "nurse_area_code", length = 20)
    private String nurseAreaCode;
    
    @Column(name = "nurse_area_name", length = 50)
    private String nurseAreaName;
    
    @Column(name = "admission_datetime", nullable = false)
    private LocalDateTime admissionDatetime;
    
    @Column(name = "discharge_datetime")
    private LocalDateTime dischargeDatetime;
    
    @Column(name = "doctor_id")
    private Long doctorId;
    
    @Column(name = "doctor_name", length = 50)
    private String doctorName;
    
    @Column(name = "nurse_level", nullable = false, length = 20)
    private String nurseLevel = "二级护理";
    
    @Column(name = "nurse_level_code", length = 10)
    private String nurseLevelCode = "02";
    
    @Column(name = "patient_status", nullable = false, length = 20)
    private String patientStatus = "在院";
    
    @Column(name = "fg_ip", nullable = false, length = 1)
    private String fgIp = "Y";
    
    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false;
    
    @Column(name = "sync_time", nullable = false)
    private LocalDateTime syncTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getVisitNo() { return visitNo; }
    public void setVisitNo(String visitNo) { this.visitNo = visitNo; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientNo() { return patientNo; }
    public void setPatientNo(String patientNo) { this.patientNo = patientNo; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getPatientHisId() { return patientHisId; }
    public void setPatientHisId(String patientHisId) { this.patientHisId = patientHisId; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    
    public String getDeptHisId() { return deptHisId; }
    public void setDeptHisId(String deptHisId) { this.deptHisId = deptHisId; }
    
    public String getBedNo() { return bedNo; }
    public void setBedNo(String bedNo) { this.bedNo = bedNo; }
    
    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }
    
    public Integer getAdmissionCount() { return admissionCount; }
    public void setAdmissionCount(Integer admissionCount) { this.admissionCount = admissionCount; }
    
    public String getNurseAreaId() { return nurseAreaId; }
    public void setNurseAreaId(String nurseAreaId) { this.nurseAreaId = nurseAreaId; }
    
    public String getNurseAreaCode() { return nurseAreaCode; }
    public void setNurseAreaCode(String nurseAreaCode) { this.nurseAreaCode = nurseAreaCode; }
    
    public String getNurseAreaName() { return nurseAreaName; }
    public void setNurseAreaName(String nurseAreaName) { this.nurseAreaName = nurseAreaName; }
    
    public LocalDateTime getAdmissionDatetime() { return admissionDatetime; }
    public void setAdmissionDatetime(LocalDateTime admissionDatetime) { this.admissionDatetime = admissionDatetime; }
    
    public LocalDateTime getDischargeDatetime() { return dischargeDatetime; }
    public void setDischargeDatetime(LocalDateTime dischargeDatetime) { this.dischargeDatetime = dischargeDatetime; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
    public String getNurseLevel() { return nurseLevel; }
    public void setNurseLevel(String nurseLevel) { this.nurseLevel = nurseLevel; }
    
    public String getNurseLevelCode() { return nurseLevelCode; }
    public void setNurseLevelCode(String nurseLevelCode) { this.nurseLevelCode = nurseLevelCode; }
    
    public String getPatientStatus() { return patientStatus; }
    public void setPatientStatus(String patientStatus) { this.patientStatus = patientStatus; }
    
    public String getFgIp() { return fgIp; }
    public void setFgIp(String fgIp) { this.fgIp = fgIp; }
    
    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }
    
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}