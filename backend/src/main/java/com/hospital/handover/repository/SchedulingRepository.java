package com.hospital.handover.repository;

import com.hospital.handover.entity.Scheduling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {
    
    List<Scheduling> findByDepartmentId(Long departmentId);
    
    List<Scheduling> findByDepartmentIdAndYearMonth(Long departmentId, String yearMonth);
    
    Optional<Scheduling> findByDepartmentIdAndYearMonthAndStatus(Long departmentId, String yearMonth, String status);
    
    boolean existsByDepartmentIdAndYearMonth(Long departmentId, String yearMonth);
    
    List<Scheduling> findByDepartmentIdOrderByYearMonthDesc(Long departmentId);
}