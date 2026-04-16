package com.hospital.handover.repository;

import com.hospital.handover.entity.SmsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsConfigRepository extends JpaRepository<SmsConfig, Long> {
    
    SmsConfig findByConfigKey(String configKey);
}