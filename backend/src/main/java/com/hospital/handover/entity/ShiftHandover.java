package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_handover")
public class ShiftHandover {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dept_id", nullable = false)
    private Long deptId;
    
    @Column(name = "dept_name", nullable = false, length = 50)
    private String deptName;
    
    @Column(name = "handover_date", nullable = false)
    private LocalDate handoverDate;
    
    @Column(nullable = false, length = 20)
    private String shift;
    
    @Column(name = "from_doctor_id", nullable = false)
    private Long fromDoctorId;
    
    @Column(name = "from_doctor_name", nullable = false, length = 50)
    private String fromDoctorName;
    
    @Column(name = "to_doctor_id")
    private Long toDoctorId;
    
    @Column(name = "to_doctor_name", length = 50)
    private String toDoctorName;
    
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";
    
    @Column(name = "handover_no", length = 10, unique = true)
    private String handoverNo;
    
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;
    
    @Column(name = "summary_json", columnDefinition = "JSON")
    private String summaryJson;
    
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
    
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    
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
    
    public String getHandoverNo() { return handoverNo; }
    public void setHandoverNo(String handoverNo) { this.handoverNo = handoverNo; }
    
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    
    public String getSummaryJson() { return summaryJson; }
    public void setSummaryJson(String summaryJson) { this.summaryJson = summaryJson; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
