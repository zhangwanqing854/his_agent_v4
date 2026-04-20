package com.hospital.handover.job;

import com.hospital.handover.service.DeptPatientInfoSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeptPatientInfoSyncJob {
    
    private static final Logger logger = LoggerFactory.getLogger(DeptPatientInfoSyncJob.class);
    
    private final DeptPatientInfoSyncService deptPatientInfoSyncService;
    
    public DeptPatientInfoSyncJob(DeptPatientInfoSyncService deptPatientInfoSyncService) {
        this.deptPatientInfoSyncService = deptPatientInfoSyncService;
    }
    
    @Scheduled(cron = "0 0 * * * ?")
    public void syncDeptPatientInfo() {
        logger.info("开始定时同步科室患者信息总览数据");
        
        try {
            DeptPatientInfoSyncService.SyncResult result = deptPatientInfoSyncService.syncDeptPatientInfo(null);
            
            if ("SUCCESS".equals(result.getStatus())) {
                logger.info("定时同步科室患者信息总览成功: 成功 {} 条, 跳过 {} 条", 
                    result.getSuccessCount(), result.getSkipCount());
            } else {
                logger.error("定时同步科室患者信息总览失败: {}", result.getMessage());
            }
        } catch (Exception e) {
            logger.error("定时同步科室患者信息总览异常: {}", e.getMessage(), e);
        }
    }
}