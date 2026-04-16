package com.hospital.handover.repository;

import com.hospital.handover.entity.RoleDuty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDutyRepository extends JpaRepository<RoleDuty, Long> {
}