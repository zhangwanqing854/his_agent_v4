package com.hospital.handover.repository;

import com.hospital.handover.entity.InterfaceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterfaceConfigRepository extends JpaRepository<InterfaceConfig, Long> {
    InterfaceConfig findByConfigCode(String configCode);
    
    List<InterfaceConfig> findBySyncOrderNotNullOrderBySyncOrderAsc();
    
    List<InterfaceConfig> findByDeptParamNotNullAndEnabledTrue();
}