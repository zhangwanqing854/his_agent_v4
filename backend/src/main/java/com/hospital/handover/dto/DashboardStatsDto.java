package com.hospital.handover.dto;

public class DashboardStatsDto {
    private Integer totalPatients;
    private Integer completedHandovers;
    private Integer pendingHandovers;
    
    public DashboardStatsDto() {}
    
    public DashboardStatsDto(Integer totalPatients, Integer completedHandovers, Integer pendingHandovers) {
        this.totalPatients = totalPatients;
        this.completedHandovers = completedHandovers;
        this.pendingHandovers = pendingHandovers;
    }
    
    public Integer getTotalPatients() { return totalPatients; }
    public void setTotalPatients(Integer totalPatients) { this.totalPatients = totalPatients; }
    
    public Integer getCompletedHandovers() { return completedHandovers; }
    public void setCompletedHandovers(Integer completedHandovers) { this.completedHandovers = completedHandovers; }
    
    public Integer getPendingHandovers() { return pendingHandovers; }
    public void setPendingHandovers(Integer pendingHandovers) { this.pendingHandovers = pendingHandovers; }
}