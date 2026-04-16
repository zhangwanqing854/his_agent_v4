package com.hospital.handover.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.handover.entity.InterfaceFieldMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldMappingProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(FieldMappingProcessor.class);
    
    public static Map<String, Object> processRecord(JsonNode sourceRecord, List<InterfaceFieldMapping> fieldMappings) {
        Map<String, Object> result = new HashMap<>();
        
        for (InterfaceFieldMapping mapping : fieldMappings) {
            String sourceField = mapping.getSourceField();
            String targetField = mapping.getTargetField();
            String transformType = mapping.getTransformType();
            String defaultValue = mapping.getDefaultValue();
            
            Object value = extractValue(sourceRecord, sourceField, transformType, defaultValue);
            
            if (value != null) {
                result.put(targetField, value);
            } else if (mapping.getIsRequired()) {
                logger.warn("Required field {} is null for source field {}", targetField, sourceField);
            }
        }
        
        return result;
    }
    
    private static Object extractValue(JsonNode sourceRecord, String sourceField, String transformType, String defaultValue) {
        JsonNode fieldNode = sourceRecord.get(sourceField);
        
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue != null ? applyTransform(defaultValue, transformType) : null;
        }
        
        String rawValue = fieldNode.asText();
        if (rawValue == null || rawValue.isEmpty() || "null".equals(rawValue)) {
            return defaultValue != null ? applyTransform(defaultValue, transformType) : null;
        }
        
        return applyTransform(rawValue, transformType);
    }
    
    private static Object applyTransform(String value, String transformType) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        if (transformType == null || "DIRECT".equals(transformType)) {
            return value;
        }
        
        switch (transformType) {
            case "DATE":
                return parseDateTime(value);
            case "NUMBER":
                return parseNumber(value);
            case "ENUM":
                return value;
            default:
                return value;
        }
    }

    public static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd"
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (pattern.equals("yyyy-MM-dd") || pattern.equals("yyyy/MM/dd")) {
                    return LocalDateTime.parse(value + " 00:00:00", 
                        DateTimeFormatter.ofPattern(pattern.replace("yyyy", "yyyy").replace("MM", "MM").replace("dd", "dd") + " HH:mm:ss"));
                }
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse datetime: {}", value);
            return null;
        }
    }
    
    private static Number parseNumber(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse number: {}", value);
            return null;
        }
    }
    
    public static String mapOrderType(String fgLong) {
        if ("Y".equals(fgLong)) {
            return "长期";
        } else if ("N".equals(fgLong)) {
            return "临时";
        }
        return fgLong;
    }
    
    public static String mapNurseLevel(String sdLevelNur) {
        Map<String, String> levelMap = new HashMap<>();
        levelMap.put("00", "特级护理");
        levelMap.put("01", "一级护理");
        levelMap.put("02", "二级护理");
        levelMap.put("03", "三级护理");
        return levelMap.getOrDefault(sdLevelNur, sdLevelNur);
    }
    
    public static String mapGender(String sdSex) {
        Map<String, String> genderMap = new HashMap<>();
        genderMap.put("0", "未知");
        genderMap.put("1", "男性");
        genderMap.put("2", "女性");
        genderMap.put("9", "未说明");
        return genderMap.getOrDefault(sdSex, sdSex);
    }
}