package com.hospital.handover.repository;

import com.hospital.handover.entity.HisStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HisStaffRepository extends JpaRepository<HisStaff, Long> {
    
    Optional<HisStaff> findByStaffCode(String staffCode);
    
    Optional<HisStaff> findByCodeUser(String codeUser);
    
    List<HisStaff> findByDepartmentId(Long departmentId);
    
    List<HisStaff> findByNameContaining(String name);
}