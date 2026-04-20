package com.hospital.handover.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.handover.client.HisSoapClient;
import com.hospital.handover.entity.DeptPatientOverview;
import com.hospital.handover.repository.DeptPatientOverviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeptPatientInfoSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeptPatientInfoSyncService.class);
    
    private final HisSoapClient hisSoapClient;
    private final DeptPatientOverviewRepository deptPatientOverviewRepository;
    
    public DeptPatientInfoSyncService(HisSoapClient hisSoapClient, DeptPatientOverviewRepository deptPatientOverviewRepository) {
        this.hisSoapClient = hisSoapClient;
        this.deptPatientOverviewRepository = deptPatientOverviewRepository;
    }
    
    @Transactional
    public SyncResult syncDeptPatientInfo(String deptCode) {
        logger.info("开始同步科室患者信息总览: {}", deptCode != null ? deptCode : "全部科室");
        
        SyncResult result = new SyncResult();
        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;
        
        try {
            JsonNode response = hisSoapClient.callGetDeptPatientInfo(deptCode);
            
            if (!response.isArray()) {
                logger.warn("SOAP响应不是数组格式: {}", response);
                result.setStatus("FAILED");
                result.setMessage("响应数据格式异常");
                return result;
            }
            
            List<DeptPatientOverview> overviews = new ArrayList<>();
            
            for (JsonNode item : response) {
                try {
                    DeptPatientOverview overview = parseDeptPatientOverview(item);
                    if (overview != null) {
                        overviews.add(overview);
                        successCount++;
                    } else {
                        skipCount++;
                    }
                } catch (Exception e) {
                    logger.warn("解析科室患者信息总览数据失败: {}, 错误: {}", item, e.getMessage());
                    skipCount++;
                }
            }
            
            for (DeptPatientOverview overview : overviews) {
                Optional<DeptPatientOverview> existing = deptPatientOverviewRepository.findByDeptCode(overview.getDeptCode());
                if (existing.isPresent()) {
                    DeptPatientOverview existingOverview = existing.get();
                    updateExistingOverview(existingOverview, overview);
                    deptPatientOverviewRepository.save(existingOverview);
                    logger.info("更新科室患者信息总览: {}", overview.getDeptCode());
                } else {
                    deptPatientOverviewRepository.save(overview);
                    logger.info("新增科室患者信息总览: {}", overview.getDeptCode());
                }
            }
            
            result.setStatus("SUCCESS");
            result.setMessage("同步成功");
            result.setTotalCount(response.size());
            result.setSuccessCount(successCount);
            result.setSkipCount(skipCount);
            result.setFailCount(failCount);
            
        } catch (Exception e) {
            logger.error("同步科室患者信息总览失败: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setMessage(e.getMessage());
            result.setFailCount(failCount);
        }
        
        logger.info("科室患者信息总览同步完成: 成功 {}, 跳过 {}, 失败 {}", successCount, skipCount, failCount);
        return result;
    }
    
    private DeptPatientOverview parseDeptPatientOverview(JsonNode item) {
        if (!item.has("code_dept") || item.get("code_dept").isNull()) {
            logger.warn("科室患者信息总览数据缺少 code_dept 字段: {}", item);
            return null;
        }
        
        String deptCode = item.get("code_dept").asText();
        if (deptCode.isEmpty()) {
            logger.warn("科室患者信息总览数据 deptCode 为空: {}", item);
            return null;
        }
        
        DeptPatientOverview overview = new DeptPatientOverview();
        overview.setDeptCode(deptCode);
        overview.setDeptId(item.has("id_dept") ? item.get("id_dept").asText() : null);
        overview.setTotalNum(item.has("totalNum") ? item.get("totalNum").asInt() : 0);
        overview.setNewInHos(item.has("newInHos") ? item.get("newInHos").asInt() : 0);
        overview.setDiseNum(item.has("diseNum") ? item.get("diseNum").asInt() : 0);
        overview.setDeathNum(item.has("deathNum") ? item.get("deathNum").asInt() : 0);
        overview.setOutNum(item.has("outNum") ? item.get("outNum").asInt() : 0);
        overview.setSurgNum(item.has("surgNum") ? item.get("surgNum").asInt() : 0);
        overview.setTransIn(item.has("transIn") ? item.get("transIn").asInt() : 0);
        overview.setTransOut(item.has("transOut") ? item.get("transOut").asInt() : 0);
        overview.setSyncedAt(LocalDateTime.now());
        
        return overview;
    }
    
    private void updateExistingOverview(DeptPatientOverview existing, DeptPatientOverview newData) {
        existing.setDeptId(newData.getDeptId());
        existing.setTotalNum(newData.getTotalNum());
        existing.setNewInHos(newData.getNewInHos());
        existing.setDiseNum(newData.getDiseNum());
        existing.setDeathNum(newData.getDeathNum());
        existing.setOutNum(newData.getOutNum());
        existing.setSurgNum(newData.getSurgNum());
        existing.setTransIn(newData.getTransIn());
        existing.setTransOut(newData.getTransOut());
        existing.setSyncedAt(newData.getSyncedAt());
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