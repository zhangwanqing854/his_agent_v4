package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dept_patient_overview", 
       uniqueConstraints = @UniqueConstraint(name = "idx_dept_code", columnNames = "dept_code"))
public class DeptPatientOverview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dept_code", nullable = false, unique = true, length = 50)
    private String deptCode;
    
    @Column(name = "dept_id", length = 50)
    private String deptId;
    
    @Column(name = "total_num")
    private Integer totalNum = 0;
    
    @Column(name = "new_in_hos")
    private Integer newInHos = 0;
    
    @Column(name = "dise_num")
    private Integer diseNum = 0;
    
    @Column(name = "death_num")
    private Integer deathNum = 0;
    
    @Column(name = "out_num")
    private Integer outNum = 0;
    
    @Column(name = "surg_num")
    private Integer surgNum = 0;
    
    @Column(name = "trans_in")
    private Integer transIn = 0;
    
    @Column(name = "trans_out")
    private Integer transOut = 0;
    
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
    
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    
    public Integer getTotalNum() { return totalNum; }
    public void setTotalNum(Integer totalNum) { this.totalNum = totalNum; }
    
    public Integer getNewInHos() { return newInHos; }
    public void setNewInHos(Integer newInHos) { this.newInHos = newInHos; }
    
    public Integer getDiseNum() { return diseNum; }
    public void setDiseNum(Integer diseNum) { this.diseNum = diseNum; }
    
    public Integer getDeathNum() { return deathNum; }
    public void setDeathNum(Integer deathNum) { this.deathNum = deathNum; }
    
    public Integer getOutNum() { return outNum; }
    public void setOutNum(Integer outNum) { this.outNum = outNum; }
    
    public Integer getSurgNum() { return surgNum; }
    public void setSurgNum(Integer surgNum) { this.surgNum = surgNum; }
    
    public Integer getTransIn() { return transIn; }
    public void setTransIn(Integer transIn) { this.transIn = transIn; }
    
    public Integer getTransOut() { return transOut; }
    public void setTransOut(Integer transOut) { this.transOut = transOut; }
    
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}