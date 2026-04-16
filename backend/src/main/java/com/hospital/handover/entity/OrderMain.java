package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_main")
public class OrderMain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "his_id", length = 50)
    private String hisId;
    
    @Column(name = "visit_id")
    private Long visitId;
    
    @Column(name = "visit_no", length = 50)
    private String visitNo;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "patient_no", length = 50)
    private String patientNo;
    
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;
    
    @Column(name = "order_name", length = 200)
    private String orderName;
    
    @Column(name = "order_type", length = 20)
    private String orderType;
    
    @Column(name = "order_category", length = 50)
    private String orderCategory;
    
    @Column(name = "service_type", length = 20)
    private String serviceType;
    
    @Column(name = "doctor_id")
    private Long doctorId;
    
    @Column(name = "doctor_name", nullable = false, length = 50)
    private String doctorName;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
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
    
    public String getHisId() { return hisId; }
    public void setHisId(String hisId) { this.hisId = hisId; }
    
    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }
    
    public String getVisitNo() { return visitNo; }
    public void setVisitNo(String visitNo) { this.visitNo = visitNo; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientNo() { return patientNo; }
    public void setPatientNo(String patientNo) { this.patientNo = patientNo; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public String getOrderName() { return orderName; }
    public void setOrderName(String orderName) { this.orderName = orderName; }
    
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    
    public String getOrderCategory() { return orderCategory; }
    public void setOrderCategory(String orderCategory) { this.orderCategory = orderCategory; }
    
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}