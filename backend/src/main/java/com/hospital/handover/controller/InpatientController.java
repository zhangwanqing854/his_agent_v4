package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.InpatientDto;
import com.hospital.handover.service.InpatientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class InpatientController {
    
    private final InpatientService inpatientService;
    
    public InpatientController(InpatientService inpatientService) {
        this.inpatientService = inpatientService;
    }
    
    @GetMapping("/inpatients")
    public ApiResponse<List<InpatientDto>> getInpatients(@RequestParam Long deptId) {
        List<InpatientDto> inpatients = inpatientService.getInpatientsByDept(deptId);
        return ApiResponse.success(inpatients);
    }
}