package com.hospital.handover.repository;

import com.hospital.handover.entity.DeptPatientOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeptPatientOverviewRepository extends JpaRepository<DeptPatientOverview, Long> {
    Optional<DeptPatientOverview> findByDeptCode(String deptCode);
    
    Optional<DeptPatientOverview> findByDeptId(String deptId);
}