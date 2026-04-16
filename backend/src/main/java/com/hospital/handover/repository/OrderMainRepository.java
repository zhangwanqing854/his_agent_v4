package com.hospital.handover.repository;

import com.hospital.handover.entity.OrderMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderMainRepository extends JpaRepository<OrderMain, Long> {
    
    Optional<OrderMain> findByOrderNo(String orderNo);
    
    @Query("SELECT o FROM OrderMain o WHERE o.visitId = :visitId " +
           "AND o.orderType = 'N' AND o.startTime >= :cutoffTime " +
           "ORDER BY o.startTime DESC")
    List<OrderMain> findTemporaryOrdersWithin24h(@Param("visitId") Long visitId,
                                                  @Param("cutoffTime") LocalDateTime cutoffTime);
}