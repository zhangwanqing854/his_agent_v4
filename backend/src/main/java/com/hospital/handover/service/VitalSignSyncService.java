package com.hospital.handover.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.handover.client.HisSoapClient;
import com.hospital.handover.entity.VitalSign;
import com.hospital.handover.repository.VitalSignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VitalSignSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(VitalSignSyncService.class);
    
    private final HisSoapClient hisSoapClient;
    private final VitalSignRepository vitalSignRepository;
    
    public VitalSignSyncService(HisSoapClient hisSoapClient, VitalSignRepository vitalSignRepository) {
        this.hisSoapClient = hisSoapClient;
        this.vitalSignRepository = vitalSignRepository;
    }
    
    @Transactional
    public SyncResult syncVitalSigns(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("开始同步生命体征数据: {} - {}", startTime, endTime);
        
        SyncResult result = new SyncResult();
        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;
        
        try {
            JsonNode response = hisSoapClient.callGetEntVt(startTime, endTime);
            
            if (!response.isArray()) {
                logger.warn("SOAP响应不是数组格式: {}", response);
                result.setStatus("FAILED");
                result.setMessage("响应数据格式异常");
                return result;
            }
            
            List<VitalSign> vitalSignsToInsert = new ArrayList<>();
            
            for (JsonNode item : response) {
                try {
                    VitalSign vitalSign = parseVitalSign(item);
                    if (vitalSign == null) {
                        skipCount++;
                        continue;
                    }
                    
                    if (vitalSign.getHisId() == null || vitalSign.getHisId().isEmpty()) {
                        logger.warn("his_id 为空，跳过: {}", item);
                        skipCount++;
                        continue;
                    }
                    
                    if (vitalSignRepository.existsByHisId(vitalSign.getHisId())) {
                        logger.info("his_id 已存在，跳过: {}", vitalSign.getHisId());
                        skipCount++;
                        continue;
                    }
                    
                    vitalSignsToInsert.add(vitalSign);
                    successCount++;
                } catch (Exception e) {
                    logger.warn("解析生命体征数据失败: {}, 错误: {}", item, e.getMessage());
                    skipCount++;
                }
            }
            
            if (!vitalSignsToInsert.isEmpty()) {
                vitalSignRepository.saveAll(vitalSignsToInsert);
                logger.info("保存生命体征数据成功: {} 条", vitalSignsToInsert.size());
            }
            
            result.setStatus("SUCCESS");
            result.setMessage("同步成功");
            result.setTotalCount(response.size());
            result.setSuccessCount(successCount);
            result.setSkipCount(skipCount);
            result.setFailCount(failCount);
            
        } catch (Exception e) {
            logger.error("同步生命体征数据失败: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setMessage(e.getMessage());
            result.setFailCount(failCount);
        }
        
        logger.info("生命体征数据同步完成: 成功 {}, 跳过 {}, 失败 {}", successCount, skipCount, failCount);
        return result;
    }
    
    private VitalSign parseVitalSign(JsonNode item) {
        if (!item.has("code_pat") || item.get("code_pat").isNull()) {
            logger.warn("生命体征数据缺少 code_pat 字段: {}", item);
            return null;
        }
        
        String patientNo = item.get("code_pat").asText();
        String hisId = item.has("his_id") && !item.get("his_id").isNull() ? item.get("his_id").asText() : null;
        String signType = item.has("name_vs_item") ? item.get("name_vs_item").asText() : "";
        String signValue = item.has("value_vs_item") ? item.get("value_vs_item").asText() : "";
        String signUnit = item.has("unit_vs_item") ? item.get("unit_vs_item").asText() : "";
        
        if (patientNo.isEmpty()) {
            logger.warn("生命体征数据 patientNo 为空: {}", item);
            return null;
        }
        
        VitalSign vitalSign = new VitalSign();
        vitalSign.setHisId(hisId);
        vitalSign.setPatientNo(patientNo);
        vitalSign.setSignType(signType);
        vitalSign.setSignValue(signValue);
        vitalSign.setSignUnit(signUnit);
        vitalSign.setSyncedAt(LocalDateTime.now());
        
        if (item.has("dt_vs_item") && !item.get("dt_vs_item").isNull()) {
            try {
                vitalSign.setRecordedAt(LocalDateTime.parse(item.get("dt_vs_item").asText(), 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                logger.warn("解析记录时间失败: {}", item.get("dt_vs_item").asText());
            }
        }
        
        return vitalSign;
    }
    
    public static class SyncResult {
        private String status;
        private String message;
        private int totalCount;
        private int successCount;
        private int skipCount;
        private int failCount;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getSkipCount() { return skipCount; }
        public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
        
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
    }
}