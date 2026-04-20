package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.DeptPatientOverviewDto;
import com.hospital.handover.service.DeptPatientOverviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dept-patient-overview")
public class DeptPatientOverviewController {
    
    private final DeptPatientOverviewService deptPatientOverviewService;
    
    public DeptPatientOverviewController(DeptPatientOverviewService deptPatientOverviewService) {
        this.deptPatientOverviewService = deptPatientOverviewService;
    }
    
    @GetMapping
    public ApiResponse<DeptPatientOverviewDto> getOverview(@RequestParam(required = false) String deptCode) {
        DeptPatientOverviewDto dto = deptPatientOverviewService.getOverviewByDeptCode(deptCode);
        return ApiResponse.success(dto);
    }
}