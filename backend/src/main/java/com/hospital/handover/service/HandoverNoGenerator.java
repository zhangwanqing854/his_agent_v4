package com.hospital.handover.service;

import com.hospital.handover.repository.ShiftHandoverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class HandoverNoGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(HandoverNoGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int MAX_RETRY = 3;
    
    private final ShiftHandoverRepository shiftHandoverRepository;
    
    public HandoverNoGenerator(ShiftHandoverRepository shiftHandoverRepository) {
        this.shiftHandoverRepository = shiftHandoverRepository;
    }
    
    @Transactional
    public String generateHandoverNo() {
        return generateHandoverNo(LocalDate.now());
    }
    
    @Transactional
    public String generateHandoverNo(LocalDate date) {
        String prefix = formatDate(date);
        
        for (int retry = 0; retry < MAX_RETRY; retry++) {
            Long maxSeq = findMaxSequenceForDate(date);
            Long nextSeq = (maxSeq == null ? 0L : maxSeq) + 1;
            String handoverNo = prefix + formatSequence(nextSeq);
            
            if (!existsHandoverNo(handoverNo)) {
                log.info("Generated handover no: {}", handoverNo);
                return handoverNo;
            }
            
            log.warn("Handover no {} already exists, retrying (attempt {})", handoverNo, retry + 1);
        }
        
        throw new RuntimeException("Failed to generate unique handover number after " + MAX_RETRY + " attempts");
    }
    
    private String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    private String formatSequence(Long seq) {
        return String.format("%03d", seq);
    }
    
    private Long findMaxSequenceForDate(LocalDate date) {
        String prefix = formatDate(date);
        return shiftHandoverRepository.findMaxSequenceByPrefix(prefix);
    }
    
    private boolean existsHandoverNo(String handoverNo) {
        return shiftHandoverRepository.existsByHandoverNo(handoverNo);
    }
}