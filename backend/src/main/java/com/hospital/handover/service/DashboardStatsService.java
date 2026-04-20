package com.hospital.handover.service;

import com.hospital.handover.dto.DashboardStatsDto;
import com.hospital.handover.entity.DeptPatientOverview;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DeptPatientOverviewRepository;
import com.hospital.handover.repository.ShiftHandoverRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DashboardStatsService {
    
    private final DeptPatientOverviewRepository deptPatientOverviewRepository;
    private final ShiftHandoverRepository shiftHandoverRepository;
    private final DepartmentRepository departmentRepository;
    
    public DashboardStatsService(DeptPatientOverviewRepository deptPatientOverviewRepository,
                                  ShiftHandoverRepository shiftHandoverRepository,
                                  DepartmentRepository departmentRepository) {
        this.deptPatientOverviewRepository = deptPatientOverviewRepository;
        this.shiftHandoverRepository = shiftHandoverRepository;
        this.departmentRepository = departmentRepository;
    }
    
    public DashboardStatsDto getStats(String deptCode, Long doctorId) {
        Integer totalPatients = 0;
        Integer completedHandovers = 0;
        Integer pendingHandovers = 0;
        
        Optional<DeptPatientOverview> overview = deptPatientOverviewRepository.findByDeptCode(deptCode);
        if (overview.isPresent()) {
            totalPatients = overview.get().getTotalNum() != null ? overview.get().getTotalNum() : 0;
        }
        
        Long deptId = getDeptIdByCode(deptCode);
        if (deptId != null && doctorId != null) {
            completedHandovers = (int) shiftHandoverRepository.countByDeptIdAndDoctorIdAndStatus(deptId, doctorId, "COMPLETED");
            pendingHandovers = (int) shiftHandoverRepository.countPendingByDeptIdAndDoctorId(deptId, doctorId);
        }
        
        return new DashboardStatsDto(totalPatients, completedHandovers, pendingHandovers);
    }
    
    private Long getDeptIdByCode(String deptCode) {
        return departmentRepository.findByCode(deptCode).map(d -> d.getId()).orElse(null);
    }
}