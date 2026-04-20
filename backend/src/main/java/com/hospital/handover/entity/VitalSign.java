package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vital_signs", indexes = @Index(name = "idx_patient_no", columnList = "patient_no"))
public class VitalSign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "patient_no", nullable = false, length = 50)
    private String patientNo;
    
    @Column(name = "sign_type", nullable = false, length = 50)
    private String signType;
    
    @Column(name = "sign_value", length = 100)
    private String signValue;
    
    @Column(name = "sign_unit", length = 20)
    private String signUnit;
    
    @Column(name = "his_id", length = 50)
    private String hisId;
    
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
    
    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;
    
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
    
    public String getPatientNo() { return patientNo; }
    public void setPatientNo(String patientNo) { this.patientNo = patientNo; }
    
    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }
    
    public String getSignValue() { return signValue; }
    public void setSignValue(String signValue) { this.signValue = signValue; }
    
    public String getSignUnit() { return signUnit; }
    public void setSignUnit(String signUnit) { this.signUnit = signUnit; }
    
    public String getHisId() { return hisId; }
    public void setHisId(String hisId) { this.hisId = hisId; }
    
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}