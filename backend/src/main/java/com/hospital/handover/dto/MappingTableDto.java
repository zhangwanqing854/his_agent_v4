package com.hospital.handover.dto;

import java.util.List;

public class MappingTableDto {
    
    private Long id;
    private Long configId;
    private String mappingCode;
    private String mappingName;
    private String targetTable;
    private String dataPath;
    private Boolean isArray;
    private Long parentMappingId;
    private String relationField;
    private Integer sortOrder;
    private List<FieldMappingDto> fieldMappings;
    
    public MappingTableDto() {}
    
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
    
    public List<FieldMappingDto> getFieldMappings() { return fieldMappings; }
    public void setFieldMappings(List<FieldMappingDto> fieldMappings) { this.fieldMappings = fieldMappings; }
}