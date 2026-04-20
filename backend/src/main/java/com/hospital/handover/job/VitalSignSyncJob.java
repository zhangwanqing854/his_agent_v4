package com.hospital.handover.job;

import com.hospital.handover.service.VitalSignSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VitalSignSyncJob {
    
    private static final Logger logger = LoggerFactory.getLogger(VitalSignSyncJob.class);
    
    private final VitalSignSyncService vitalSignSyncService;
    
    public VitalSignSyncJob(VitalSignSyncService vitalSignSyncService) {
        this.vitalSignSyncService = vitalSignSyncService;
    }
    
    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    public void syncVitalSigns() {
        logger.info("开始定时同步生命体征数据");
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(6);
        
        try {
            VitalSignSyncService.SyncResult result = vitalSignSyncService.syncVitalSigns(startTime, endTime);
            
            if ("SUCCESS".equals(result.getStatus())) {
                logger.info("定时同步生命体征数据成功: 成功 {} 条, 跳过 {} 条", 
                    result.getSuccessCount(), result.getSkipCount());
            } else {
                logger.error("定时同步生命体征数据失败: {}", result.getMessage());
            }
        } catch (Exception e) {
            logger.error("定时同步生命体征数据异常: {}", e.getMessage(), e);
        }
    }
}