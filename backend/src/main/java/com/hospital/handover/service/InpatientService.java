package com.hospital.handover.service;

import com.hospital.handover.dto.InpatientDto;
import com.hospital.handover.entity.Visit;
import com.hospital.handover.repository.VisitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InpatientService {
    
    private final VisitRepository visitRepository;
    
    public InpatientService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }
    
    public List<InpatientDto> getInpatientsByDept(Long deptId) {
        List<Visit> visits = visitRepository.findByDepartmentIdAndPatientStatus(deptId, "在院");
        return visits.stream().map(this::toDto).collect(Collectors.toList());
    }
    
    private InpatientDto toDto(Visit visit) {
        InpatientDto dto = new InpatientDto();
        dto.setId(visit.getId());
        dto.setBedNo(visit.getBedNo() != null ? visit.getBedNo() : "");
        dto.setPatientNo(visit.getPatientNo());
        dto.setPatientName(visit.getPatientName());
        dto.setNurseLevel(visit.getNurseLevel());
        dto.setIsCritical(visit.getIsCritical() != null ? visit.getIsCritical() : false);
        dto.setAdmissionDatetime(visit.getAdmissionDatetime());
        dto.setPatientStatus(visit.getPatientStatus());
        dto.setDeptName(visit.getDeptName());
        return dto;
    }
}