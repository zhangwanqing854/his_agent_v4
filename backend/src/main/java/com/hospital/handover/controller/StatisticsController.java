package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.StatisticsDto;
import com.hospital.handover.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<ApiResponse<StatisticsDto>> getDepartmentStatistics(
            @PathVariable Long deptId) {
        StatisticsDto stats = statisticsService.getDepartmentStatistics(deptId);
        if (stats == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "科室不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<StatisticsDto>>> getAllDepartmentStatistics() {
        List<StatisticsDto> stats = statisticsService.getAllDepartmentStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}