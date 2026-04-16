package com.hospital.handover.repository;

import com.hospital.handover.entity.DiagnosisMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosisMainRepository extends JpaRepository<DiagnosisMain, Long> {
    Optional<DiagnosisMain> findByHisId(String hisId);
    
    List<DiagnosisMain> findByVisitIdOrderByDiagnosisTimeDesc(Long visitId);
}