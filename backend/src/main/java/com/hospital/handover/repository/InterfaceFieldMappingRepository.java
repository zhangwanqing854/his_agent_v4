package com.hospital.handover.repository;

import com.hospital.handover.entity.InterfaceFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterfaceFieldMappingRepository extends JpaRepository<InterfaceFieldMapping, Long> {
    List<InterfaceFieldMapping> findByMappingTableId(Long mappingTableId);
    
    @Modifying
    @Query("DELETE FROM InterfaceFieldMapping f WHERE f.mappingTableId = :mappingTableId")
    void deleteByMappingTableId(@Param("mappingTableId") Long mappingTableId);
}