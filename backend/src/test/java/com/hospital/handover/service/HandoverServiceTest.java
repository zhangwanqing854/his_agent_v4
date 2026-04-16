package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.*;
import com.hospital.handover.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandoverServiceTest {

    private ShiftHandoverRepository shiftHandoverRepository;
    private HandoverPatientRepository handoverPatientRepository;
    private VisitRepository visitRepository;
    private PatientRepository patientRepository;
    private OrderMainRepository orderMainRepository;
    private DepartmentRepository departmentRepository;
    private HandoverService handoverService;

    @BeforeEach
    void setUp() {
        shiftHandoverRepository = mock(ShiftHandoverRepository.class);
        handoverPatientRepository = mock(HandoverPatientRepository.class);
        visitRepository = mock(VisitRepository.class);
        patientRepository = mock(PatientRepository.class);
        orderMainRepository = mock(OrderMainRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        handoverService = new HandoverService(shiftHandoverRepository, handoverPatientRepository,
            visitRepository, patientRepository, orderMainRepository, departmentRepository);
    }

    @Test
    void testGetHandoverList() {
        ShiftHandover handover = new ShiftHandover();
        handover.setId(1L);
        handover.setDeptId(1L);
        handover.setDeptName("心内科");
        handover.setHandoverDate(LocalDate.now());
        handover.setShift("白班");
        handover.setStatus("DRAFT");
        
        when(shiftHandoverRepository.findByDeptIdOrderByHandoverDateDesc(1L))
            .thenReturn(Arrays.asList(handover));
        when(handoverPatientRepository.findByHandoverIdOrderByBedNo(1L))
            .thenReturn(Collections.emptyList());
        
        var result = handoverService.getHandoverList(1L);
        
        assertEquals(1, result.size());
        assertEquals("心内科", result.get(0).getDeptName());
        assertEquals(0, result.get(0).getPatientCount());
    }

    @Test
    void testGetHandoverById() {
        ShiftHandover handover = new ShiftHandover();
        handover.setId(1L);
        handover.setDeptId(1L);
        handover.setDeptName("心内科");
        handover.setHandoverDate(LocalDate.now());
        handover.setShift("白班");
        handover.setFromDoctorId(1L);
        handover.setFromDoctorName("张医生");
        handover.setStatus("DRAFT");
        
        when(shiftHandoverRepository.findById(1L)).thenReturn(Optional.of(handover));
        when(handoverPatientRepository.findByHandoverIdOrderByBedNo(1L))
            .thenReturn(Collections.emptyList());
        
        HandoverDto result = handoverService.getHandoverById(1L);
        
        assertNotNull(result);
        assertEquals("心内科", result.getDeptName());
        assertEquals("张医生", result.getFromDoctorName());
    }

    @Test
    void testGetHandoverByIdNotFound() {
        when(shiftHandoverRepository.findById(999L)).thenReturn(Optional.empty());
        
        HandoverDto result = handoverService.getHandoverById(999L);
        
        assertNull(result);
    }

    @Test
    void testGetHandoverPatients() {
        HandoverPatient hp = new HandoverPatient();
        hp.setId(1L);
        hp.setHandoverId(1L);
        hp.setVisitId(1L);
        hp.setPatientId(1L);
        hp.setPatientName("张三");
        hp.setGender("男");
        hp.setAge(45);
        hp.setBedNo("A101");
        hp.setFilterReason("24小时内新入院");
        hp.setCurrentCondition("药物治疗: 王医生");
        
        when(handoverPatientRepository.findByHandoverIdOrderByBedNo(1L))
            .thenReturn(Arrays.asList(hp));
        
        var result = handoverService.getHandoverPatients(1L);
        
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getPatientName());
        assertEquals("24小时内新入院", result.get(0).getFilterReason());
        assertEquals("药物治疗: 王医生", result.get(0).getCurrentCondition());
    }

    @Test
    void testCreateHandover() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("心内科");
        
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("张三");
        patient.setGender("男");
        patient.setAge(45);
        
        Visit visit = new Visit();
        visit.setId(1L);
        visit.setPatientId(1L);
        visit.setDepartmentId(1L);
        visit.setBedNo("A101");
        visit.setAdmissionDatetime(LocalDateTime.now().minusHours(12));
        visit.setNurseLevelCode("01");
        
        ShiftHandover savedHandover = new ShiftHandover();
        savedHandover.setId(1L);
        savedHandover.setDeptId(1L);
        savedHandover.setDeptName("心内科");
        savedHandover.setHandoverDate(LocalDate.now());
        savedHandover.setShift("白班");
        savedHandover.setFromDoctorId(1L);
        savedHandover.setStatus("DRAFT");
        
        OrderMain order = new OrderMain();
        order.setId(1L);
        order.setOrderCategory("药物治疗");
        order.setDoctorName("王医生");
        order.setOrderType("N");
        order.setStartTime(LocalDateTime.now().minusHours(10));
        
        HandoverCreateRequest request = new HandoverCreateRequest();
        request.setDeptId(1L);
        request.setHandoverDate(LocalDate.now());
        request.setShift("白班");
        request.setFromDoctorId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(shiftHandoverRepository.save(any())).thenReturn(savedHandover);
        when(visitRepository.findHandoverPatients(eq(1L), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(visit));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(orderMainRepository.findTemporaryOrdersWithin24h(eq(1L), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(order));
        when(handoverPatientRepository.save(any())).thenReturn(new HandoverPatient());
        when(handoverPatientRepository.findByHandoverIdOrderByBedNo(1L))
            .thenReturn(Collections.emptyList());
        
        HandoverDto result = handoverService.createHandover(request);
        
        assertNotNull(result);
        assertEquals("心内科", result.getDeptName());
        assertEquals("白班", result.getShift());
    }

    @Test
    void testCreateHandoverDeptNotFound() {
        HandoverCreateRequest request = new HandoverCreateRequest();
        request.setDeptId(999L);
        request.setHandoverDate(LocalDate.now());
        request.setShift("白班");
        request.setFromDoctorId(1L);
        
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());
        
        HandoverDto result = handoverService.createHandover(request);
        
        assertNull(result);
    }

    @Test
    void testFilterReasonNewAdmissionOnly() {
        Visit visit = new Visit();
        visit.setAdmissionDatetime(LocalDateTime.now().minusHours(12));
        visit.setNurseLevelCode("02");
        
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        ShiftHandover handover = new ShiftHandover();
        handover.setId(1L);
        
        when(shiftHandoverRepository.save(any())).thenReturn(handover);
        when(visitRepository.findHandoverPatients(eq(1L), any())).thenReturn(Arrays.asList(visit));
        when(patientRepository.findById(any())).thenReturn(Optional.of(new Patient()));
        when(orderMainRepository.findTemporaryOrdersWithin24h(any(), any())).thenReturn(Collections.emptyList());
        when(handoverPatientRepository.save(any())).thenAnswer(invocation -> {
            HandoverPatient hp = invocation.getArgument(0);
            assertEquals("24小时内新入院", hp.getFilterReason());
            return hp;
        });
        
        HandoverCreateRequest request = new HandoverCreateRequest();
        request.setDeptId(1L);
        request.setHandoverDate(LocalDate.now());
        request.setShift("白班");
        request.setFromDoctorId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(new Department()));
        
        handoverService.createHandover(request);
    }

    @Test
    void testFilterReasonLevel1NursingOnly() {
        Visit visit = new Visit();
        visit.setAdmissionDatetime(LocalDateTime.now().minusHours(48));
        visit.setNurseLevelCode("01");
        
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        when(shiftHandoverRepository.save(any())).thenReturn(new ShiftHandover());
        when(visitRepository.findHandoverPatients(eq(1L), any())).thenReturn(Arrays.asList(visit));
        when(patientRepository.findById(any())).thenReturn(Optional.of(new Patient()));
        when(orderMainRepository.findTemporaryOrdersWithin24h(any(), any())).thenReturn(Collections.emptyList());
        when(handoverPatientRepository.save(any())).thenAnswer(invocation -> {
            HandoverPatient hp = invocation.getArgument(0);
            assertEquals("一级护理", hp.getFilterReason());
            return hp;
        });
        
        HandoverCreateRequest request = new HandoverCreateRequest();
        request.setDeptId(1L);
        request.setHandoverDate(LocalDate.now());
        request.setShift("白班");
        request.setFromDoctorId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(new Department()));
        
        handoverService.createHandover(request);
    }
}