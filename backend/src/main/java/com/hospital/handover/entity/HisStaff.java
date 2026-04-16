package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "his_staff")
public class HisStaff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "staff_code", nullable = false, unique = true, length = 50)
    private String staffCode;
    
    @Column(name = "code_user", length = 50)
    private String codeUser;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(name = "name_user", length = 50)
    private String nameUser;
    
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(length = 50)
    private String title;
    
    @Column(name = "title_code", length = 50)
    private String titleCode;
    
    @Column(name = "employment_status_code", length = 10)
    private String employmentStatusCode;
    
    @Column(name = "employment_status", length = 50)
    private String employmentStatus;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "sync_time")
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
    
    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }
    
    public String getCodeUser() { return codeUser; }
    public void setCodeUser(String codeUser) { this.codeUser = codeUser; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getNameUser() { return nameUser; }
    public void setNameUser(String nameUser) { this.nameUser = nameUser; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getTitleCode() { return titleCode; }
    public void setTitleCode(String titleCode) { this.titleCode = titleCode; }
    
    public String getEmploymentStatusCode() { return employmentStatusCode; }
    public void setEmploymentStatusCode(String employmentStatusCode) { this.employmentStatusCode = employmentStatusCode; }
    
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}