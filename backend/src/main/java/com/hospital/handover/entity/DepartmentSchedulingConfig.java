package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_scheduling_config")
public class DepartmentSchedulingConfig {
    
    @Id
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(name = "staff_order", nullable = false, columnDefinition = "JSON")
    private String staffOrder;
    
    @Column(name = "last_position")
    private Integer lastPosition;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getStaffOrder() { return staffOrder; }
    public void setStaffOrder(String staffOrder) { this.staffOrder = staffOrder; }
    
    public Integer getLastPosition() { return lastPosition; }
    public void setLastPosition(Integer lastPosition) { this.lastPosition = lastPosition; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}