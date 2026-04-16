package com.hospital.handover.repository;

import com.hospital.handover.entity.DoctorDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorDepartmentRepository extends JpaRepository<DoctorDepartment, Long> {
    
    Optional<DoctorDepartment> findByDoctorIdAndDepartmentId(Long doctorId, Long departmentId);
    
    List<DoctorDepartment> findByDoctorId(Long doctorId);
    
    List<DoctorDepartment> findByDepartmentId(Long departmentId);
    
    @Modifying
    @Query("DELETE FROM DoctorDepartment dd WHERE dd.doctorId = :doctorId")
    void deleteByDoctorId(Long doctorId);
    
    @Modifying
    @Query("DELETE FROM DoctorDepartment dd WHERE dd.doctorId = :doctorId AND dd.departmentId = :departmentId")
    void deleteByDoctorIdAndDepartmentId(Long doctorId, Long departmentId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE doctor_department d JOIN (SELECT id, ROW_NUMBER() OVER (PARTITION BY doctor_id ORDER BY id) as rn FROM doctor_department) ranked ON d.id = ranked.id SET d.is_primary = (ranked.rn = 1)", nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.flushMode", value = "AUTO"))
    int recalculateIsPrimary();
}