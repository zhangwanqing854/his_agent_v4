package com.hospital.handover.controller;

import com.hospital.handover.dto.*;
import com.hospital.handover.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitDto>>> getPatientList(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String bedNo,
            @RequestParam(required = false) String visitStatus) {
        
        PatientFilterRequest filter = new PatientFilterRequest();
        filter.setDepartmentId(departmentId);
        filter.setPatientName(patientName);
        filter.setBedNo(bedNo);
        filter.setVisitStatus(visitStatus);
        
        List<VisitDto> patients = patientService.getPatientList(filter);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientDto>> getPatientById(@PathVariable Long id) {
        PatientDto patient = patientService.getPatientById(id);
        if (patient == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "患者不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping("/{id}/visit")
    public ResponseEntity<ApiResponse<VisitDto>> getVisitByPatientId(@PathVariable Long id) {
        VisitDto visit = patientService.getVisitByPatientId(id);
        if (visit == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "就诊信息不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(visit));
    }
}