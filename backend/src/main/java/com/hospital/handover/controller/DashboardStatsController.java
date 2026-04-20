package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.DashboardStatsDto;
import com.hospital.handover.service.DashboardStatsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardStatsController {
    
    private final DashboardStatsService dashboardStatsService;
    
    public DashboardStatsController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }
    
    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDto> getStats(@RequestParam String deptCode, @RequestParam Long doctorId) {
        DashboardStatsDto stats = dashboardStatsService.getStats(deptCode, doctorId);
        return ApiResponse.success(stats);
    }
}