package com.hospital.handover.service;

import com.hospital.handover.dto.StatisticsDto;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.HandoverPatient;
import com.hospital.handover.entity.ShiftHandover;
import com.hospital.handover.entity.Visit;
import com.hospital.handover.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceTest {

    private DepartmentRepository departmentRepository;
    private ShiftHandoverRepository shiftHandoverRepository;
    private HandoverPatientRepository handoverPatientRepository;
    private VisitRepository visitRepository;
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        departmentRepository = mock(DepartmentRepository.class);
        shiftHandoverRepository = mock(ShiftHandoverRepository.class);
        handoverPatientRepository = mock(HandoverPatientRepository.class);
        visitRepository = mock(VisitRepository.class);
        statisticsService = new StatisticsService(departmentRepository, shiftHandoverRepository,
            handoverPatientRepository, visitRepository);
    }

    @Test
    void testGetDepartmentStatistics() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("心内科");
        
        ShiftHandover handover1 = new ShiftHandover();
        handover1.setId(1L);
        handover1.setStatus("COMPLETED");
        
        ShiftHandover handover2 = new ShiftHandover();
        handover2.setId(2L);
        handover2.setStatus("DRAFT");
        
        HandoverPatient patient1 = new HandoverPatient();
        patient1.setId(1L);
        patient1.setHandoverId(1L);
        
        HandoverPatient patient2 = new HandoverPatient();
        patient2.setId(2L);
        patient2.setHandoverId(1L);
        
        HandoverPatient patient3 = new HandoverPatient();
        patient3.setId(3L);
        patient3.setHandoverId(2L);
        
        Visit visit1 = new Visit();
        visit1.setAdmissionDatetime(LocalDateTime.now().minusHours(12));
        visit1.setNurseLevelCode("02");
        
        Visit visit2 = new Visit();
        visit2.setAdmissionDatetime(LocalDateTime.now().minusHours(48));
        visit2.setNurseLevelCode("01");
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(shiftHandoverRepository.findByDeptIdOrderByHandoverDateDesc(1L))
            .thenReturn(Arrays.asList(handover1, handover2));
        when(handoverPatientRepository.findByHandoverIdOrderByBedNo(1L))
            .thenReturn(Arrays.asList(patient1, patient2));
        when(handoverPatientRepository.findByHandoverIdOrderByBedNo(2L))
            .thenReturn(Arrays.asList(patient3));
        when(visitRepository.findHandoverPatients(eq(1L), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(visit1, visit2));
        
        StatisticsDto result = statisticsService.getDepartmentStatistics(1L);
        
        assertNotNull(result);
        assertEquals("心内科", result.getDeptName());
        assertEquals(2, result.getTotalHandovers());
        assertEquals(1, result.getCompletedHandovers());
        assertEquals(1, result.getDraftHandovers());
        assertEquals(3, result.getTotalPatients());
        assertEquals(1, result.getNewAdmissionPatients());
        assertEquals(1, result.getLevel1NursingPatients());
    }

    @Test
    void testGetDepartmentStatisticsDeptNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        
        StatisticsDto result = statisticsService.getDepartmentStatistics(999L);
        
        assertNull(result);
    }

    @Test
    void testGetDepartmentStatisticsEmptyHandovers() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("心内科");
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(shiftHandoverRepository.findByDeptIdOrderByHandoverDateDesc(1L))
            .thenReturn(Collections.emptyList());
        when(visitRepository.findHandoverPatients(eq(1L), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        
        StatisticsDto result = statisticsService.getDepartmentStatistics(1L);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalHandovers());
        assertEquals(0, result.getTotalPatients());
        assertEquals(0, result.getNewAdmissionPatients());
        assertEquals(0, result.getLevel1NursingPatients());
    }

    @Test
    void testGetAllDepartmentStatistics() {
        Department dept1 = new Department();
        dept1.setId(1L);
        dept1.setName("心内科");
        
        Department dept2 = new Department();
        dept2.setId(2L);
        dept2.setName("神经内科");
        
        ShiftHandover handover = new ShiftHandover();
        handover.setId(1L);
        handover.setStatus("COMPLETED");
        
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(dept1, dept2));
        when(shiftHandoverRepository.findByDeptIdOrderByHandoverDateDesc(1L))
            .thenReturn(Arrays.asList(handover));
        when(shiftHandoverRepository.findByDeptIdOrderByHandoverDateDesc(2L))
            .thenReturn(Collections.emptyList());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept1));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(dept2));
        when(handoverPatientRepository.findByHandoverIdOrderByBedNo(1L))
            .thenReturn(Collections.emptyList());
        when(visitRepository.findHandoverPatients(any(), any())).thenReturn(Collections.emptyList());
        
        var result = statisticsService.getAllDepartmentStatistics();
        
        assertEquals(2, result.size());
        assertEquals("心内科", result.get(0).getDeptName());
        assertEquals("神经内科", result.get(1).getDeptName());
    }
}