package com.hospital.handover.repository;

import com.hospital.handover.entity.DepartmentSchedulingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentSchedulingConfigRepository extends JpaRepository<DepartmentSchedulingConfig, Long> {
    
    Optional<DepartmentSchedulingConfig> findByDepartmentId(Long departmentId);
}