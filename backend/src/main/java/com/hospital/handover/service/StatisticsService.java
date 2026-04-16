package com.hospital.handover.service;

import com.hospital.handover.dto.StatisticsDto;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.HandoverPatient;
import com.hospital.handover.entity.ShiftHandover;
import com.hospital.handover.entity.Visit;
import com.hospital.handover.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StatisticsService {

    private final DepartmentRepository departmentRepository;
    private final ShiftHandoverRepository shiftHandoverRepository;
    private final HandoverPatientRepository handoverPatientRepository;
    private final VisitRepository visitRepository;

    public StatisticsService(DepartmentRepository departmentRepository,
                            ShiftHandoverRepository shiftHandoverRepository,
                            HandoverPatientRepository handoverPatientRepository,
                            VisitRepository visitRepository) {
        this.departmentRepository = departmentRepository;
        this.shiftHandoverRepository = shiftHandoverRepository;
        this.handoverPatientRepository = handoverPatientRepository;
        this.visitRepository = visitRepository;
    }

    public StatisticsDto getDepartmentStatistics(Long deptId) {
        Optional<Department> dept = departmentRepository.findById(deptId);
        if (!dept.isPresent()) {
            return null;
        }

        StatisticsDto dto = new StatisticsDto();
        dto.setDeptId(deptId);
        dto.setDeptName(dept.get().getName());

        List<ShiftHandover> handovers = shiftHandoverRepository.findByDeptIdOrderByHandoverDateDesc(deptId);
        dto.setTotalHandovers(handovers.size());
        
        int completed = 0;
        int draft = 0;
        int totalPatients = 0;
        
        for (ShiftHandover h : handovers) {
            if ("COMPLETED".equals(h.getStatus())) {
                completed++;
            } else if ("DRAFT".equals(h.getStatus())) {
                draft++;
            }
            List<HandoverPatient> patients = handoverPatientRepository.findByHandoverIdOrderByBedNo(h.getId());
            totalPatients += patients.size();
        }
        
        dto.setCompletedHandovers(completed);
        dto.setDraftHandovers(draft);
        dto.setTotalPatients(totalPatients);

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        LocalDateTime now = LocalDateTime.now();
        List<Visit> visits = visitRepository.findHandoverPatients(deptId, cutoff, now);
        
        int newAdmission = 0;
        int level1Nursing = 0;
        
        for (Visit v : visits) {
            boolean isNew = v.getAdmissionDatetime() != null && v.getAdmissionDatetime().isAfter(cutoff);
            boolean isLevel1 = "01".equals(v.getNurseLevelCode());
            
            if (isNew) newAdmission++;
            if (isLevel1) level1Nursing++;
        }
        
        dto.setNewAdmissionPatients(newAdmission);
        dto.setLevel1NursingPatients(level1Nursing);

        return dto;
    }

    public List<StatisticsDto> getAllDepartmentStatistics() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
            .map(d -> getDepartmentStatistics(d.getId()))
            .filter(d -> d != null)
            .toList();
    }
}