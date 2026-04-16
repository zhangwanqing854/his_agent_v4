package com.hospital.handover.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interface_mapping_table")
public class InterfaceMappingTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_id", nullable = false)
    private Long configId;
    
    @Column(name = "mapping_code", nullable = false, length = 50)
    private String mappingCode;
    
    @Column(name = "mapping_name", nullable = false, length = 100)
    private String mappingName;
    
    @Column(name = "target_table", nullable = false, length = 50)
    private String targetTable;
    
    @Column(name = "data_path", length = 200)
    private String dataPath;
    
    @Column(name = "is_array")
    private Boolean isArray = false;
    
    @Column(name = "parent_mapping_id")
    private Long parentMappingId;
    
    @Column(name = "relation_field", length = 50)
    private String relationField;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
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
    
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    
    public String getMappingCode() { return mappingCode; }
    public void setMappingCode(String mappingCode) { this.mappingCode = mappingCode; }
    
    public String getMappingName() { return mappingName; }
    public void setMappingName(String mappingName) { this.mappingName = mappingName; }
    
    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }
    
    public String getDataPath() { return dataPath; }
    public void setDataPath(String dataPath) { this.dataPath = dataPath; }
    
    public Boolean getIsArray() { return isArray; }
    public void setIsArray(Boolean isArray) { this.isArray = isArray; }
    
    public Long getParentMappingId() { return parentMappingId; }
    public void setParentMappingId(Long parentMappingId) { this.parentMappingId = parentMappingId; }
    
    public String getRelationField() { return relationField; }
    public void setRelationField(String relationField) { this.relationField = relationField; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}