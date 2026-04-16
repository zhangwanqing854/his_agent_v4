package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.DutyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duties")
public class DutyController {

    private final DutyService dutyService;

    public DutyController(DutyService dutyService) {
        this.dutyService = dutyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DutyDto>>> getAllDuties() {
        List<DutyDto> duties = dutyService.getAllDuties();
        return ResponseEntity.ok(ApiResponse.success(duties));
    }
}