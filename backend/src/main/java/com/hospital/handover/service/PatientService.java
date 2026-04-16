package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.Patient;
import com.hospital.handover.entity.Visit;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.PatientRepository;
import com.hospital.handover.repository.VisitRepository;
import com.hospital.handover.util.AgeCalculator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final DepartmentRepository departmentRepository;

    public PatientService(PatientRepository patientRepository,
                         VisitRepository visitRepository,
                         DepartmentRepository departmentRepository) {
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<VisitDto> getPatientList(PatientFilterRequest filter) {
        List<Visit> visits = visitRepository.findAll();
        List<VisitDto> result = new ArrayList<>();
        
        for (Visit visit : visits) {
            if (filter != null) {
                if (filter.getDepartmentId() != null) {
                    if (!filter.getDepartmentId().equals(visit.getDepartmentId())) {
                        continue;
                    }
                }
                if (filter.getVisitStatus() != null && !filter.getVisitStatus().isEmpty()) {
                    if (!filter.getVisitStatus().equals(visit.getPatientStatus())) {
                        continue;
                    }
                }
                if (filter.getBedNo() != null && !filter.getBedNo().isEmpty()) {
                    if (visit.getBedNo() == null || !visit.getBedNo().contains(filter.getBedNo())) {
                        continue;
                    }
                }
            }
            
            VisitDto dto = toVisitDto(visit);
            
            if (filter != null && filter.getPatientName() != null && !filter.getPatientName().isEmpty()) {
                if (dto.getPatientName() == null || !dto.getPatientName().contains(filter.getPatientName())) {
                    continue;
                }
            }
            
            result.add(dto);
        }
        
        return result;
    }

    public PatientDto getPatientById(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        return patient.map(this::toPatientDto).orElse(null);
    }

    public VisitDto getVisitByPatientId(Long patientId) {
        List<Visit> visits = visitRepository.findAll().stream()
            .filter(v -> v.getPatientId().equals(patientId))
            .collect(Collectors.toList());
        
        if (visits.isEmpty()) {
            return null;
        }
        
        Visit visit = visits.get(0);
        return toVisitDto(visit);
    }

    private PatientDto toPatientDto(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setId(patient.getId());
        dto.setPatientNo(patient.getPatientNo());
        dto.setName(patient.getName());
        dto.setGender(patient.getGender());
        dto.setAge(AgeCalculator.calculateAgeOrDefault(patient.getBirthDate(), patient.getAge()));
        dto.setIdCard(patient.getIdCard());
        dto.setPhone(patient.getPhone());
        dto.setAddress(patient.getAddress());
        return dto;
    }

    private VisitDto toVisitDto(Visit visit) {
        VisitDto dto = new VisitDto();
        dto.setId(visit.getId());
        dto.setVisitNo(visit.getVisitNo());
        dto.setPatientId(visit.getPatientId());
        dto.setDepartmentId(visit.getDepartmentId());
        dto.setBedNo(visit.getBedNo());
        dto.setNursingLevel(visit.getNurseLevel());
        dto.setVisitStatus(visit.getPatientStatus());
        dto.setAdmitTime(visit.getAdmissionDatetime());
        dto.setDischargeTime(visit.getDischargeDatetime());
        
        Optional<Patient> patient = patientRepository.findById(visit.getPatientId());
        patient.ifPresent(p -> dto.setPatientName(p.getName()));
        
        if (visit.getDepartmentId() != null) {
            Optional<Department> dept = departmentRepository.findById(visit.getDepartmentId());
            dept.ifPresent(d -> dto.setDepartmentName(d.getName()));
        }
        
        return dto;
    }
}