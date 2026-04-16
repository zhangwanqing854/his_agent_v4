package com.hospital.handover.repository;

import com.hospital.handover.entity.SyncRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncRecordRepository extends JpaRepository<SyncRecord, Long> {
}