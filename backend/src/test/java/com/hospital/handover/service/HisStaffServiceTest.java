package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.HisStaff;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.HisStaffRepository;
import com.hospital.handover.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HisStaffServiceTest {

    private HisStaffRepository hisStaffRepository;
    private DepartmentRepository departmentRepository;
    private UserRepository userRepository;
    private DoctorDepartmentRepository doctorDepartmentRepository;
    private HisStaffService hisStaffService;

    @BeforeEach
    void setUp() {
        hisStaffRepository = mock(HisStaffRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        userRepository = mock(UserRepository.class);
        doctorDepartmentRepository = mock(DoctorDepartmentRepository.class);
        hisStaffService = new HisStaffService(hisStaffRepository, departmentRepository, userRepository, doctorDepartmentRepository);
    }

    @Test
    void testGetAllStaff() {
        HisStaff staff1 = new HisStaff();
        staff1.setId(1L);
        staff1.setStaffCode("D001");
        staff1.setName("张医生");
        staff1.setDepartmentId(1L);
        staff1.setTitle("主治医师");
        staff1.setSyncTime(LocalDateTime.now());
        
        HisStaff staff2 = new HisStaff();
        staff2.setId(2L);
        staff2.setStaffCode("D002");
        staff2.setName("李医生");
        staff2.setDepartmentId(1L);
        
        when(hisStaffRepository.findAll()).thenReturn(Arrays.asList(staff1, staff2));
        
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("心内科");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        
        var result = hisStaffService.getAllStaff();
        
        assertEquals(2, result.size());
        assertEquals("张医生", result.get(0).getName());
        assertEquals("心内科", result.get(0).getDepartmentName());
    }

    @Test
    void testGetUnlinkedStaff() {
        HisStaff staff1 = new HisStaff();
        staff1.setId(1L);
        staff1.setStaffCode("D001");
        staff1.setName("张医生");
        staff1.setDepartmentId(1L);
        
        HisStaff staff2 = new HisStaff();
        staff2.setId(2L);
        staff2.setStaffCode("D002");
        staff2.setName("李医生");
        staff2.setDepartmentId(1L);
        
        HisStaff staff3 = new HisStaff();
        staff3.setId(3L);
        staff3.setStaffCode("D003");
        staff3.setName("王医生");
        staff3.setDepartmentId(1L);
        
        when(hisStaffRepository.findAll()).thenReturn(Arrays.asList(staff1, staff2, staff3));
        
        User user = new User();
        user.setId(1L);
        user.setHisStaffId(1L);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("心内科");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        
        var result = hisStaffService.getUnlinkedStaff();
        
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(s -> s.getName().equals("张医生")));
    }

    @Test
    void testGetAllStaffEmpty() {
        when(hisStaffRepository.findAll()).thenReturn(Collections.emptyList());
        
        var result = hisStaffService.getAllStaff();
        
        assertTrue(result.isEmpty());
    }
}