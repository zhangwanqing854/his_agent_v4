package com.hospital.handover.repository;

import com.hospital.handover.entity.SchedulingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulingDetailRepository extends JpaRepository<SchedulingDetail, Long> {
    
    List<SchedulingDetail> findBySchedulingId(Long schedulingId);
    
    List<SchedulingDetail> findBySchedulingIdOrderByDutyDateAsc(Long schedulingId);
    
    Optional<SchedulingDetail> findBySchedulingIdAndDutyDate(Long schedulingId, LocalDate dutyDate);
    
    void deleteBySchedulingId(Long schedulingId);
    
    @Modifying
    @Query("DELETE FROM SchedulingDetail sd WHERE sd.schedulingId = :schedulingId")
    void deleteAllBySchedulingId(@Param("schedulingId") Long schedulingId);

    @Query("SELECT sd.staffId, hs.name FROM SchedulingDetail sd " +
           "JOIN Scheduling s ON sd.schedulingId = s.id " +
           "JOIN HisStaff hs ON sd.staffId = hs.id " +
           "WHERE s.departmentId = :deptId " +
           "AND s.yearMonth = :yearMonth " +
           "AND sd.dutyDate = :today " +
           "AND s.status = 'published'")
    List<Object[]> findTodayDutyStaff(@Param("deptId") Long departmentId,
                                       @Param("yearMonth") String yearMonth,
                                       @Param("today") LocalDate today);
}