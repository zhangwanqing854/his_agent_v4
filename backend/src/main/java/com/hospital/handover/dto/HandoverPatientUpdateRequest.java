package com.hospital.handover.dto;

public class HandoverPatientUpdateRequest {
    
    private String vitals;
    private String currentCondition;
    private String observationItems;
    
    public HandoverPatientUpdateRequest() {}
    
    public String getVitals() { return vitals; }
    public void setVitals(String vitals) { this.vitals = vitals; }
    
    public String getCurrentCondition() { return currentCondition; }
    public void setCurrentCondition(String currentCondition) { this.currentCondition = currentCondition; }
    
    public String getObservationItems() { return observationItems; }
    public void setObservationItems(String observationItems) { this.observationItems = observationItems; }
}