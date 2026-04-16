package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "handover_patient")
public class HandoverPatient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "handover_id", nullable = false)
    private Long handoverId;
    
    @Column(name = "visit_id", nullable = false)
    private Long visitId;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "filter_reason", nullable = false, length = 50)
    private String filterReason;
    
    @Column(name = "bed_no", length = 20)
    private String bedNo;
    
    @Column(name = "patient_name", nullable = false, length = 50)
    private String patientName;
    
    @Column(length = 10)
    private String gender;
    
    private Integer age;
    
    @Column(length = 500)
    private String diagnosis;
    
    @Column(columnDefinition = "TEXT")
    private String vitals;
    
    @Column(name = "current_condition", columnDefinition = "TEXT")
    private String currentCondition;
    
    @Column(name = "observation_items", columnDefinition = "TEXT")
    private String observationItems;
    
    @Column(name = "mews_score")
    private Integer mewsScore;
    
    @Column(name = "braden_score")
    private Integer bradenScore;
    
    @Column(name = "fall_risk")
    private Integer fallRisk;
    
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
    
    public Long getHandoverId() { return handoverId; }
    public void setHandoverId(Long handoverId) { this.handoverId = handoverId; }
    
    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getFilterReason() { return filterReason; }
    public void setFilterReason(String filterReason) { this.filterReason = filterReason; }
    
    public String getBedNo() { return bedNo; }
    public void setBedNo(String bedNo) { this.bedNo = bedNo; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    
    public String getVitals() { return vitals; }
    public void setVitals(String vitals) { this.vitals = vitals; }
    
    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }
    
    public String getObservationItems() { return observationItems; }
    public void setObservationItems(String observationItems) { this.observationItems = observationItems; }
    
    public Integer getMewsScore() { return mewsScore; }
    public void setMewsScore(Integer mewsScore) { this.mewsScore = mewsScore; }
    
    public Integer getBradenScore() { return bradenScore; }
    public void setBradenScore(Integer bradenScore) { this.bradenScore = bradenScore; }
    
    public Integer getFallRisk() { return fallRisk; }
    public void setFallRisk(Integer fallRisk) { this.fallRisk = fallRisk; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
