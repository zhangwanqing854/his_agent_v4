package com.hospital.handover.dto;

import java.time.LocalDateTime;

public class DeptPatientOverviewDto {
    private String deptCode;
    private String deptId;
    private Integer totalNum;
    private Integer diseNum;
    private Integer newInHos;
    private Integer transIn;
    private Integer transOut;
    private Integer outNum;
    private Integer surgNum;
    private Integer deathNum;
    private LocalDateTime syncedAt;
    
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }
    
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    
    public Integer getTotalNum() { return totalNum; }
    public void setTotalNum(Integer totalNum) { this.totalNum = totalNum; }
    
    public Integer getDiseNum() { return diseNum; }
    public void setDiseNum(Integer diseNum) { this.diseNum = diseNum; }
    
    public Integer getNewInHos() { return newInHos; }
    public void setNewInHos(Integer newInHos) { this.newInHos = newInHos; }
    
    public Integer getTransIn() { return transIn; }
    public void setTransIn(Integer transIn) { this.transIn = transIn; }
    
    public Integer getTransOut() { return transOut; }
    public void setTransOut(Integer transOut) { this.transOut = transOut; }
    
    public Integer getOutNum() { return outNum; }
    public void setOutNum(Integer outNum) { this.outNum = outNum; }
    
    public Integer getSurgNum() { return surgNum; }
    public void setSurgNum(Integer surgNum) { this.surgNum = surgNum; }
    
    public Integer getDeathNum() { return deathNum; }
    public void setDeathNum(Integer deathNum) { this.deathNum = deathNum; }
    
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}