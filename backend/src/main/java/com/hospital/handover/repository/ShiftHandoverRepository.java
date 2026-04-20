package com.hospital.handover.repository;

import com.hospital.handover.entity.ShiftHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftHandoverRepository extends JpaRepository<ShiftHandover, Long> {
    
    List<ShiftHandover> findByDeptIdOrderByHandoverDateDesc(Long deptId);
    
    Optional<ShiftHandover> findByDeptIdAndHandoverDateAndShift(Long deptId, LocalDate handoverDate, String shift);
    
    @Query("SELECT h FROM ShiftHandover h WHERE h.deptId = :deptId " +
           "AND h.handoverDate >= :startDate AND h.handoverDate <= :endDate " +
           "ORDER BY h.handoverDate DESC, h.shift")
    List<ShiftHandover> findByDeptIdAndDateRange(@Param("deptId") Long deptId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT MAX(CAST(SUBSTRING(h.handoverNo, 7, 3) AS LONG)) FROM ShiftHandover h " +
           "WHERE h.handoverNo LIKE CONCAT(:prefix, '%')")
    Long findMaxSequenceByPrefix(@Param("prefix") String prefix);
    
    boolean existsByHandoverNo(String handoverNo);
    
    @Query("SELECT COUNT(h) FROM ShiftHandover h WHERE h.deptId = :deptId AND h.status = :status")
    long countByDeptIdAndStatus(@Param("deptId") Long deptId, @Param("status") String status);
    
    @Query("SELECT COUNT(h) FROM ShiftHandover h WHERE h.deptId = :deptId AND h.status IN ('PENDING', 'TRANSFERRING')")
    long countPendingByDeptId(@Param("deptId") Long deptId);
    
    @Query("SELECT COUNT(h) FROM ShiftHandover h WHERE h.deptId = :deptId AND (h.fromDoctorId = :doctorId OR h.toDoctorId = :doctorId) AND h.status = :status")
    long countByDeptIdAndDoctorIdAndStatus(@Param("deptId") Long deptId, @Param("doctorId") Long doctorId, @Param("status") String status);
    
    @Query("SELECT COUNT(h) FROM ShiftHandover h WHERE h.deptId = :deptId AND h.toDoctorId = :doctorId AND h.status IN ('PENDING', 'TRANSFERRING')")
    long countPendingByDeptIdAndDoctorId(@Param("deptId") Long deptId, @Param("doctorId") Long doctorId);
}