package com.hospital.handover.repository;

import com.hospital.handover.entity.HandoverPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandoverPatientRepository extends JpaRepository<HandoverPatient, Long> {
    
    List<HandoverPatient> findByHandoverIdOrderByBedNo(Long handoverId);
    
    void deleteByHandoverId(Long handoverId);
}