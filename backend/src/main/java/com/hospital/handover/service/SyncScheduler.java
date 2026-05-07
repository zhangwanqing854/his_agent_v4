package com.hospital.handover.service;

import com.hospital.handover.entity.InterfaceConfig;
import com.hospital.handover.repository.InterfaceConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SyncScheduler.class);

    private final InterfaceConfigRepository configRepository;
    private final SyncService syncService;

    public SyncScheduler(InterfaceConfigRepository configRepository, SyncService syncService) {
        this.configRepository = configRepository;
        this.syncService = syncService;
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void executeScheduledSync() {
        logger.info("========== 开始执行定时同步任务 ==========");
        
        List<InterfaceConfig> configs = configRepository.findBySyncOrderNotNullOrderBySyncOrderAsc();
        
        if (configs.isEmpty()) {
            logger.info("没有需要定时同步的接口配置");
            return;
        }
        
        for (InterfaceConfig config : configs) {
            try {
                logger.info("[{}/{}] 开始同步: {} ({})", 
                    config.getSyncOrder(), configs.size(), config.getConfigName(), config.getConfigCode());
                
                syncService.executeSync(config.getId(), null, "SCHEDULED");
                
                logger.info("[{}/{}] 同步完成: {}", 
                    config.getSyncOrder(), configs.size(), config.getConfigName());
                    
            } catch (Exception e) {
                logger.error("[{}/{}] 同步失败: {} - {}", 
                    config.getSyncOrder(), configs.size(), config.getConfigName(), e.getMessage());
            }
        }
        
        logger.info("========== 定时同步任务执行完毕 ==========");
    }
}