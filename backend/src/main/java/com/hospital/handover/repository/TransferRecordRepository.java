package com.hospital.handover.repository;

import com.hospital.handover.entity.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, Long> {
    List<TransferRecord> findByVisitNo(String visitNo);

    @Query("SELECT COUNT(t) FROM TransferRecord t WHERE t.fromDeptId = :deptId " +
           "AND t.transferTime >= :cutoffTime")
    Integer countTransferOut24h(@Param("deptId") Long departmentId, 
                                @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(t) FROM TransferRecord t WHERE t.toDeptId = :deptId " +
           "AND t.transferTime >= :cutoffTime")
    Integer countTransferIn24h(@Param("deptId") Long departmentId, 
                               @Param("cutoffTime") LocalDateTime cutoffTime);
}