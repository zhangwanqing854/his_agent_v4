package com.hospital.handover.repository;

import com.hospital.handover.entity.HandoverTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandoverTodoRepository extends JpaRepository<HandoverTodo, Long> {
    
    List<HandoverTodo> findByHandoverIdOrderByCreatedAtAsc(Long handoverId);
    
    List<HandoverTodo> findByHandoverIdAndStatus(Long handoverId, String status);
}