package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnosis_main")
public class DiagnosisMain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "visit_id")
    private Long visitId;
    
    @Column(name = "visit_no", nullable = false, length = 50)
    private String visitNo;
    
    @Column(name = "patient_id")
    private Long patientId;
    
    @Column(name = "patient_no", nullable = false, length = 50)
    private String patientNo;
    
    @Column(name = "his_id", length = 50)
    private String hisId;
    
    @Column(name = "diagnosis_type", nullable = false, length = 50)
    private String diagnosisType;
    
    @Column(name = "diagnosis_time", nullable = false)
    private LocalDateTime diagnosisTime;
    
    @Column(name = "doctor_id")
    private Long doctorId;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(name = "sync_time", nullable = false)
    private LocalDateTime syncTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }
    
    public String getVisitNo() { return visitNo; }
    public void setVisitNo(String visitNo) { this.visitNo = visitNo; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientNo() { return patientNo; }
    public void setPatientNo(String patientNo) { this.patientNo = patientNo; }
    
    public String getHisId() { return hisId; }
    public void setHisId(String hisId) { this.hisId = hisId; }
    
    public String getDiagnosisType() { return diagnosisType; }
    public void setDiagnosisType(String diagnosisType) { this.diagnosisType = diagnosisType; }
    
    public LocalDateTime getDiagnosisTime() { return diagnosisTime; }
    public void setDiagnosisTime(LocalDateTime diagnosisTime) { this.diagnosisTime = diagnosisTime; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}