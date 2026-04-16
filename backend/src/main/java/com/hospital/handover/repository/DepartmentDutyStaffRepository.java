package com.hospital.handover.repository;

import com.hospital.handover.entity.DepartmentDutyStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentDutyStaffRepository extends JpaRepository<DepartmentDutyStaff, Long> {
    
    List<DepartmentDutyStaff> findByDepartmentIdOrderByDisplayOrderAsc(Long departmentId);
    
    Optional<DepartmentDutyStaff> findByDepartmentIdAndStaffId(Long departmentId, Long staffId);
    
    boolean existsByDepartmentIdAndStaffId(Long departmentId, Long staffId);
    
    @Modifying
    @Query("DELETE FROM DepartmentDutyStaff d WHERE d.departmentId = :departmentId AND d.staffId = :staffId")
    void deleteByDepartmentIdAndStaffId(Long departmentId, Long staffId);
    
    @Modifying
    @Query("DELETE FROM DepartmentDutyStaff d WHERE d.departmentId = :departmentId")
    void deleteByDepartmentId(Long departmentId);
    
    long countByDepartmentId(Long departmentId);
}