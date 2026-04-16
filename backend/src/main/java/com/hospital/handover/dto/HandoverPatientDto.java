package com.hospital.handover.dto;

public class HandoverPatientDto {
    
    private Long id;
    private Long visitId;
    private Long patientId;
    private String patientName;
    private String gender;
    private Integer age;
    private String bedNo;
    private String diagnosis;
    private String filterReason;
    private String currentCondition;
    private String vitals;
    private String observationItems;
    private Integer mewsScore;
    private Integer bradenScore;
    private Integer fallRisk;
    
    public HandoverPatientDto() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getBedNo() { return bedNo; }
    public void setBedNo(String bedNo) { this.bedNo = bedNo; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    
    public String getFilterReason() { return filterReason; }
    public void setFilterReason(String filterReason) { this.filterReason = filterReason; }
    
    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }
    
    public String getVitals() { return vitals; }
    public void setVitals(String vitals) { this.vitals = vitals; }
    
    public String getObservationItems() { return observationItems; }
    public void setObservationItems(String observationItems) { this.observationItems = observationItems; }
    
    public Integer getMewsScore() { return mewsScore; }
    public void setMewsScore(Integer mewsScore) { this.mewsScore = mewsScore; }
    
    public Integer getBradenScore() { return bradenScore; }
    public void setBradenScore(Integer bradenScore) { this.bradenScore = bradenScore; }
    
    public Integer getFallRisk() { return fallRisk; }
    public void setFallRisk(Integer fallRisk) { this.fallRisk = fallRisk; }
}
