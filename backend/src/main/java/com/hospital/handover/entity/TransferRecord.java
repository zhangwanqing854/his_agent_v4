package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_record")
public class TransferRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "visit_id", nullable = false)
    private Long visitId;
    
    @Column(name = "visit_no", nullable = false, length = 50)
    private String visitNo;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "patient_no", length = 50)
    private String patientNo;
    
    @Column(name = "from_dept_id")
    private Long fromDeptId;
    
    @Column(name = "from_dept_name", length = 50)
    private String fromDeptName;
    
    @Column(name = "to_dept_id", nullable = false)
    private Long toDeptId;
    
    @Column(name = "to_dept_name", nullable = false, length = 50)
    private String toDeptName;
    
    @Column(name = "transfer_time", nullable = false)
    private LocalDateTime transferTime;
    
    @Column(name = "transfer_type", nullable = false, length = 20)
    private String transferType;
    
    @Column(length = 500)
    private String reason;
    
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
    
    public Long getFromDeptId() { return fromDeptId; }
    public void setFromDeptId(Long fromDeptId) { this.fromDeptId = fromDeptId; }
    
    public String getFromDeptName() { return fromDeptName; }
    public void setFromDeptName(String fromDeptName) { this.fromDeptName = fromDeptName; }
    
    public Long getToDeptId() { return toDeptId; }
    public void setToDeptId(Long toDeptId) { this.toDeptId = toDeptId; }
    
    public String getToDeptName() { return toDeptName; }
    public void setToDeptName(String toDeptName) { this.toDeptName = toDeptName; }
    
    public LocalDateTime getTransferTime() { return transferTime; }
    public void setTransferTime(LocalDateTime transferTime) { this.transferTime = transferTime; }
    
    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}