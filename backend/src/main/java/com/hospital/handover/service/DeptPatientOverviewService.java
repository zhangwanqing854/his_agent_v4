package com.hospital.handover.service;

import com.hospital.handover.dto.DeptPatientOverviewDto;
import com.hospital.handover.entity.DeptPatientOverview;
import com.hospital.handover.repository.DeptPatientOverviewRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeptPatientOverviewService {
    
    private final DeptPatientOverviewRepository deptPatientOverviewRepository;
    
    public DeptPatientOverviewService(DeptPatientOverviewRepository deptPatientOverviewRepository) {
        this.deptPatientOverviewRepository = deptPatientOverviewRepository;
    }
    
    public DeptPatientOverviewDto getOverviewByDeptCode(String deptCode) {
        Optional<DeptPatientOverview> overview = deptPatientOverviewRepository.findByDeptCode(deptCode);
        
        DeptPatientOverviewDto dto = new DeptPatientOverviewDto();
        
        if (overview.isPresent()) {
            DeptPatientOverview entity = overview.get();
            dto.setDeptCode(entity.getDeptCode());
            dto.setDeptId(entity.getDeptId());
            dto.setTotalNum(entity.getTotalNum() != null ? entity.getTotalNum() : 0);
            dto.setDiseNum(entity.getDiseNum() != null ? entity.getDiseNum() : 0);
            dto.setNewInHos(entity.getNewInHos() != null ? entity.getNewInHos() : 0);
            dto.setTransIn(entity.getTransIn() != null ? entity.getTransIn() : 0);
            dto.setTransOut(entity.getTransOut() != null ? entity.getTransOut() : 0);
            dto.setOutNum(entity.getOutNum() != null ? entity.getOutNum() : 0);
            dto.setSurgNum(entity.getSurgNum() != null ? entity.getSurgNum() : 0);
            dto.setDeathNum(entity.getDeathNum() != null ? entity.getDeathNum() : 0);
            dto.setSyncedAt(entity.getSyncedAt());
        } else {
            dto.setDeptCode(deptCode);
            dto.setTotalNum(0);
            dto.setDiseNum(0);
            dto.setNewInHos(0);
            dto.setTransIn(0);
            dto.setTransOut(0);
            dto.setOutNum(0);
            dto.setSurgNum(0);
            dto.setDeathNum(0);
            dto.setSyncedAt(null);
        }
        
        return dto;
    }
}