package com.hospital.handover.dto;

public class StatisticsDto {
    
    private Long deptId;
    private String deptName;
    private Integer totalHandovers;
    private Integer completedHandovers;
    private Integer draftHandovers;
    private Integer totalPatients;
    private Integer newAdmissionPatients;
    private Integer level1NursingPatients;
    
    public StatisticsDto() {}
    
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    
    public Integer getTotalHandovers() { return totalHandovers; }
    public void setTotalHandovers(Integer totalHandovers) { this.totalHandovers = totalHandovers; }
    
    public Integer getCompletedHandovers() { return completedHandovers; }
    public void setCompletedHandovers(Integer completedHandovers) { this.completedHandovers = completedHandovers; }
    
    public Integer getDraftHandovers() { return draftHandovers; }
    public void setDraftHandovers(Integer draftHandovers) { this.draftHandovers = draftHandovers; }
    
    public Integer getTotalPatients() { return totalPatients; }
    public void setTotalPatients(Integer totalPatients) { this.totalPatients = totalPatients; }
    
    public Integer getNewAdmissionPatients() { return newAdmissionPatients; }
    public void setNewAdmissionPatients(Integer newAdmissionPatients) { this.newAdmissionPatients = newAdmissionPatients; }
    
    public Integer getLevel1NursingPatients() { return level1NursingPatients; }
    public void setLevel1NursingPatients(Integer level1NursingPatients) { this.level1NursingPatients = level1NursingPatients; }
}