package com.hospital.handover.repository;

import com.hospital.handover.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {
    Duty findByCode(String code);
}