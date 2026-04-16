package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient")
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "patient_no", nullable = false, unique = true, length = 50)
    private String patientNo;
    
    @Column(length = 50)
    private String name;
    
    @Column(length = 10)
    private String gender;
    
    @Column(name = "sd_sex", length = 10)
    private String sdSex;
    
    @Column(name = "sd_nation", length = 10)
    private String sdNation;
    
    @Column(name = "name_nation", length = 50)
    private String nameNation;
    
    @Column(name = "sd_country", length = 10)
    private String sdCountry;
    
    @Column(name = "name_country", length = 50)
    private String nameCountry;
    
    private Integer age;
    
    @Column(name = "birth_date")
    private LocalDateTime birthDate;
    
    @Column(name = "id_card", length = 20)
    private String idCard;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 255)
    private String address;
    
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
    
    public String getPatientNo() { return patientNo; }
    public void setPatientNo(String patientNo) { this.patientNo = patientNo; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getSdSex() { return sdSex; }
    public void setSdSex(String sdSex) { this.sdSex = sdSex; }
    
    public String getSdNation() { return sdNation; }
    public void setSdNation(String sdNation) { this.sdNation = sdNation; }
    
    public String getNameNation() { return nameNation; }
    public void setNameNation(String nameNation) { this.nameNation = nameNation; }
    
    public String getSdCountry() { return sdCountry; }
    public void setSdCountry(String sdCountry) { this.sdCountry = sdCountry; }
    
    public String getNameCountry() { return nameCountry; }
    public void setNameCountry(String nameCountry) { this.nameCountry = nameCountry; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public LocalDateTime getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDateTime birthDate) { this.birthDate = birthDate; }
    
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDateTime getSyncTime() { return syncTime; }
    public void setSyncTime(LocalDateTime syncTime) { this.syncTime = syncTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}