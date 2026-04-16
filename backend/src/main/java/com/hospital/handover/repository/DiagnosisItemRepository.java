package com.hospital.handover.repository;

import com.hospital.handover.entity.DiagnosisItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosisItemRepository extends JpaRepository<DiagnosisItem, Long> {
    Optional<DiagnosisItem> findByMainIdAndDiagnosisCode(Long mainId, String diagnosisCode);
    
    List<DiagnosisItem> findByMainIdAndIsMainOrderBySortOrder(Long mainId, Boolean isMain);
}