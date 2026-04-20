package com.hospital.handover.repository;

import com.hospital.handover.entity.VitalSign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {
    List<VitalSign> findByPatientNo(String patientNo);
    List<VitalSign> findByRecordedAtBetween(LocalDateTime start, LocalDateTime end);
    List<VitalSign> findByPatientNoAndSignType(String patientNo, String signType);
    List<VitalSign> findByPatientNoAndRecordedAtAfter(String patientNo, LocalDateTime startTime);
    
    VitalSign findByHisId(String hisId);
    boolean existsByHisId(String hisId);
    
    void deleteByPatientNo(String patientNo);
    void deleteByPatientNoIn(List<String> patientNos);
}