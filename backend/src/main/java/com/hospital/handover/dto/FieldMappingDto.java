package com.hospital.handover.dto;

public class FieldMappingDto {
    
    private Long id;
    private Long mappingTableId;
    private String sourceField;
    private String targetField;
    private String transformType;
    private String transformConfig;
    private String defaultValue;
    private Boolean isRequired;
    private Integer sortOrder;
    
    public FieldMappingDto() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMappingTableId() { return mappingTableId; }
    public void setMappingTableId(Long mappingTableId) { this.mappingTableId = mappingTableId; }
    
    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }
    
    public String getTargetField() { return targetField; }
    public void setTargetField(String targetField) { this.targetField = targetField; }
    
    public String getTransformType() { return transformType; }
    public void setTransformType(String transformType) { this.transformType = transformType; }
    
    public String getTransformConfig() { return transformConfig; }
    public void setTransformConfig(String transformConfig) { this.transformConfig = transformConfig; }
    
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    
    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}