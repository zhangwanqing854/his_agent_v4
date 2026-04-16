package com.hospital.handover.repository;

import com.hospital.handover.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByItemId(String itemId);
    
    List<OrderItem> findByMainId(Long mainId);
}