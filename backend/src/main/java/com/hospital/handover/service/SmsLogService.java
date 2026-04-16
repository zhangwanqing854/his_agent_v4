package com.hospital.handover.service;

import com.hospital.handover.entity.SmsLog;
import com.hospital.handover.repository.SmsLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SmsLogService {
    
    private final SmsLogRepository smsLogRepository;
    
    public SmsLogService(SmsLogRepository smsLogRepository) {
        this.smsLogRepository = smsLogRepository;
    }
    
    public void logSuccess(Long handoverId, String phone, String content) {
        SmsLog log = new SmsLog();
        log.setHandoverId(handoverId);
        log.setPhone(phone);
        log.setContent(content);
        log.setStatus("SUCCESS");
        log.setSentAt(LocalDateTime.now());
        smsLogRepository.save(log);
    }
    
    public void logFailure(Long handoverId, String phone, String content, String errorMessage) {
        SmsLog log = new SmsLog();
        log.setHandoverId(handoverId);
        log.setPhone(phone);
        log.setContent(content);
        log.setStatus("FAILED");
        log.setErrorMessage(errorMessage);
        smsLogRepository.save(log);
    }
}