package com.hospital.handover.repository;

import com.hospital.handover.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    
    Optional<Visit> findByVisitNo(String visitNo);
    
    List<Visit> findByDepartmentId(Long departmentId);
    
    @Query("SELECT v FROM Visit v WHERE v.departmentId = :deptId " +
           "AND v.fgIp = 'Y' " +
           "AND (v.dischargeDatetime IS NULL OR v.dischargeDatetime > :now) " +
           "AND (v.admissionDatetime >= :cutoffTime OR v.nurseLevelCode = '01')")
    List<Visit> findHandoverPatients(@Param("deptId") Long departmentId, 
                                      @Param("cutoffTime") LocalDateTime cutoffTime,
                                      @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.departmentId = :deptId " +
           "AND v.fgIp = 'Y' AND (v.dischargeDatetime IS NULL OR v.dischargeDatetime > :now)")
    Integer countTotalPatients(@Param("deptId") Long departmentId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.departmentId = :deptId " +
           "AND v.admissionDatetime >= :cutoffTime AND v.fgIp = 'Y' " +
           "AND (v.dischargeDatetime IS NULL OR v.dischargeDatetime > :now)")
    Integer countAdmission24h(@Param("deptId") Long departmentId, 
                              @Param("cutoffTime") LocalDateTime cutoffTime,
                              @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.departmentId = :deptId " +
           "AND v.dischargeDatetime >= :cutoffTime AND v.fgIp = 'N'")
    Integer countDischarge24h(@Param("deptId") Long departmentId, 
                              @Param("cutoffTime") LocalDateTime cutoffTime);
    
    List<Visit> findByDepartmentIdAndPatientStatus(Long departmentId, String patientStatus);
}