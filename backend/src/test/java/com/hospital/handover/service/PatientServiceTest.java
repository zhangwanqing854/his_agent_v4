package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.Patient;
import com.hospital.handover.entity.Visit;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.PatientRepository;
import com.hospital.handover.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    private PatientRepository patientRepository;
    private VisitRepository visitRepository;
    private DepartmentRepository departmentRepository;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientRepository = mock(PatientRepository.class);
        visitRepository = mock(VisitRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        patientService = new PatientService(patientRepository, visitRepository, departmentRepository);
    }

    @Test
    void testGetPatientList() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("张三");
        
        Visit visit = new Visit();
        visit.setId(1L);
        visit.setVisitNo("V001");
        visit.setPatientId(1L);
        visit.setDepartmentId(1L);
        visit.setBedNo("A101");
        visit.setPatientStatus("在院");
        
        when(visitRepository.findAll()).thenReturn(Arrays.asList(visit));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("心内科");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        
        var result = patientService.getPatientList(null);
        
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getPatientName());
        assertEquals("心内科", result.get(0).getDepartmentName());
    }

    @Test
    void testGetPatientListWithFilter() {
        Visit visit1 = new Visit();
        visit1.setId(1L);
        visit1.setPatientId(1L);
        visit1.setDepartmentId(1L);
        visit1.setPatientStatus("在院");
        
        Visit visit2 = new Visit();
        visit2.setId(2L);
        visit2.setPatientId(2L);
        visit2.setDepartmentId(2L);
        visit2.setPatientStatus("出院");
        
        when(visitRepository.findAll()).thenReturn(Arrays.asList(visit1, visit2));
        
        PatientFilterRequest filter = new PatientFilterRequest();
        filter.setVisitStatus("在院");
        
        var result = patientService.getPatientList(filter);
        
        assertEquals(1, result.size());
    }

    @Test
    void testGetPatientById() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setPatientNo("P001");
        patient.setName("张三");
        patient.setGender("男");
        patient.setAge(45);
        
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        
        PatientDto result = patientService.getPatientById(1L);
        
        assertNotNull(result);
        assertEquals("张三", result.getName());
        assertEquals("男", result.getGender());
    }

    @Test
    void testGetPatientByIdNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());
        
        PatientDto result = patientService.getPatientById(999L);
        
        assertNull(result);
    }

    @Test
    void testGetVisitByPatientId() {
        Visit visit = new Visit();
        visit.setId(1L);
        visit.setVisitNo("V001");
        visit.setPatientId(1L);
        visit.setDepartmentId(1L);
        visit.setBedNo("A101");
        
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("张三");
        
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("心内科");
        
        when(visitRepository.findAll()).thenReturn(Arrays.asList(visit));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        
        VisitDto result = patientService.getVisitByPatientId(1L);
        
        assertNotNull(result);
        assertEquals("V001", result.getVisitNo());
        assertEquals("张三", result.getPatientName());
        assertEquals("心内科", result.getDepartmentName());
    }

    @Test
    void testGetVisitByPatientIdNotFound() {
        when(visitRepository.findAll()).thenReturn(Collections.emptyList());
        
        VisitDto result = patientService.getVisitByPatientId(999L);
        
        assertNull(result);
    }
}