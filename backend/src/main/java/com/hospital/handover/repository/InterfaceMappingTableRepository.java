package com.hospital.handover.repository;

import com.hospital.handover.entity.InterfaceMappingTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterfaceMappingTableRepository extends JpaRepository<InterfaceMappingTable, Long> {
    List<InterfaceMappingTable> findByConfigId(Long configId);
    
    @Modifying
    @Query("DELETE FROM InterfaceMappingTable t WHERE t.configId = :configId")
    void deleteByConfigId(@Param("configId") Long configId);
}