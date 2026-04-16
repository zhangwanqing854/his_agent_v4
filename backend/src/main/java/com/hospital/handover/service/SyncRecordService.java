package com.hospital.handover.service;

import com.hospital.handover.entity.InterfaceConfig;
import com.hospital.handover.entity.SyncRecord;
import com.hospital.handover.repository.InterfaceConfigRepository;
import com.hospital.handover.repository.SyncRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SyncRecordService {

    private static final Logger logger = LoggerFactory.getLogger(SyncRecordService.class);

    private final SyncRecordRepository syncRecordRepository;
    private final InterfaceConfigRepository configRepository;

    public SyncRecordService(SyncRecordRepository syncRecordRepository,
                            InterfaceConfigRepository configRepository) {
        this.syncRecordRepository = syncRecordRepository;
        this.configRepository = configRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSyncRecord(Long configId, LocalDateTime startTime, LocalDateTime endTime,
                               int totalCount, int insertCount, int updateCount,
                               int skipCount, int failCount, String errorMessage,
                               String requestData, long durationMs) {
        try {
            Optional<InterfaceConfig> configOpt = configRepository.findById(configId);
            if (!configOpt.isPresent()) {
                return;
            }

            InterfaceConfig config = configOpt.get();

            SyncRecord record = new SyncRecord();
            record.setConfigId(configId);
            record.setConfigCode(config.getConfigCode());
            record.setConfigName(config.getConfigName());
            record.setSyncType("MANUAL");
            record.setSyncStartTime(startTime);
            record.setSyncEndTime(endTime);
            record.setActualStartTime(startTime);
            record.setActualEndTime(endTime);
            record.setSyncStatus(failCount > 0 ? "PARTIAL" : "SUCCESS");
            record.setRecordCount(totalCount);
            record.setInsertCount(insertCount);
            record.setUpdateCount(updateCount);
            record.setSkipCount(skipCount);
            record.setFailCount(failCount);
            record.setErrorMessage(errorMessage);
            record.setRequestData(requestData);
            record.setDurationMs((int) durationMs);

            syncRecordRepository.save(record);
        } catch (Exception e) {
            logger.error("Error saving sync record: {}", e.getMessage());
        }
    }
}