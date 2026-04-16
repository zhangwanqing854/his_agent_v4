package com.hospital.handover.dto;

public class HandoverStatsDto {
    private Integer totalPatients;
    private Integer admission;
    private Integer transferOut;
    private Integer discharge;
    private Integer transferIn;

    public Integer getTotalPatients() { return totalPatients; }
    public void setTotalPatients(Integer totalPatients) { this.totalPatients = totalPatients; }

    public Integer getAdmission() { return admission; }
    public void setAdmission(Integer admission) { this.admission = admission; }

    public Integer getTransferOut() { return transferOut; }
    public void setTransferOut(Integer transferOut) { this.transferOut = transferOut; }

    public Integer getDischarge() { return discharge; }
    public void setDischarge(Integer discharge) { this.discharge = discharge; }

    public Integer getTransferIn() { return transferIn; }
    public void setTransferIn(Integer transferIn) { this.transferIn = transferIn; }
}