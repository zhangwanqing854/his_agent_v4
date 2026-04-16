package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.DepartmentDutyStaff;
import com.hospital.handover.entity.DoctorDepartment;
import com.hospital.handover.entity.HisStaff;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentDutyStaffRepository;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.HisStaffRepository;
import com.hospital.handover.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DutyStaffService {

    private final DepartmentDutyStaffRepository dutyStaffRepository;
    private final HisStaffRepository hisStaffRepository;
    private final UserRepository userRepository;
    private final DoctorDepartmentRepository doctorDepartmentRepository;
    private final DepartmentRepository departmentRepository;

    public DutyStaffService(DepartmentDutyStaffRepository dutyStaffRepository,
                           HisStaffRepository hisStaffRepository,
                           UserRepository userRepository,
                           DoctorDepartmentRepository doctorDepartmentRepository,
                           DepartmentRepository departmentRepository) {
        this.dutyStaffRepository = dutyStaffRepository;
        this.hisStaffRepository = hisStaffRepository;
        this.userRepository = userRepository;
        this.doctorDepartmentRepository = doctorDepartmentRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentDutyStaffDto> getDutyStaffList(Long departmentId) {
        if (departmentId == null) {
            return new ArrayList<>();
        }
        
        List<DepartmentDutyStaff> dutyStaffList = dutyStaffRepository.findByDepartmentIdOrderByDisplayOrderAsc(departmentId);
        
        return dutyStaffList.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<DepartmentDutyStaffDto> addDutyStaff(Long departmentId, List<Long> staffIds) {
        if (departmentId == null || staffIds == null || staffIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DepartmentDutyStaff> addedList = new ArrayList<>();
        int maxOrder = getMaxDisplayOrder(departmentId);
        
        for (Long staffId : staffIds) {
            if (!dutyStaffRepository.existsByDepartmentIdAndStaffId(departmentId, staffId)) {
                DepartmentDutyStaff dutyStaff = new DepartmentDutyStaff();
                dutyStaff.setDepartmentId(departmentId);
                dutyStaff.setStaffId(staffId);
                dutyStaff.setDisplayOrder(maxOrder + addedList.size() + 1);
                addedList.add(dutyStaffRepository.save(dutyStaff));
            }
        }
        
        return addedList.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void removeDutyStaff(Long departmentId, Long staffId) {
        dutyStaffRepository.deleteByDepartmentIdAndStaffId(departmentId, staffId);
    }

    @Transactional
    public List<DepartmentDutyStaffDto> updateOrder(Long departmentId, List<Long> staffIds) {
        if (departmentId == null || staffIds == null) {
            return new ArrayList<>();
        }
        
        for (int i = 0; i < staffIds.size(); i++) {
            Long staffId = staffIds.get(i);
            DepartmentDutyStaff dutyStaff = dutyStaffRepository.findByDepartmentIdAndStaffId(departmentId, staffId).orElse(null);
            if (dutyStaff != null) {
                dutyStaff.setDisplayOrder(i);
                dutyStaffRepository.save(dutyStaff);
            }
        }
        
        return getDutyStaffList(departmentId);
    }

    @Transactional
    public List<DepartmentDutyStaffDto> initializeDutyStaff(Long departmentId) {
        if (departmentId == null) {
            return new ArrayList<>();
        }
        
        dutyStaffRepository.deleteByDepartmentId(departmentId);
        
        List<DoctorDepartment> doctorDepts = doctorDepartmentRepository.findByDepartmentId(departmentId);
        List<Long> userIds = doctorDepts.stream()
            .map(DoctorDepartment::getDoctorId)
            .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<User> users = userRepository.findAllById(userIds);
        List<Long> hisStaffIds = users.stream()
            .map(User::getHisStaffId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
        
        if (hisStaffIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return addDutyStaff(departmentId, hisStaffIds);
    }

    private int getMaxDisplayOrder(Long departmentId) {
        List<DepartmentDutyStaff> list = dutyStaffRepository.findByDepartmentIdOrderByDisplayOrderAsc(departmentId);
        if (list.isEmpty()) {
            return 0;
        }
        return list.get(list.size() - 1).getDisplayOrder();
    }

    private DepartmentDutyStaffDto toDto(DepartmentDutyStaff dutyStaff) {
        DepartmentDutyStaffDto dto = new DepartmentDutyStaffDto();
        dto.setId(dutyStaff.getId());
        dto.setDepartmentId(dutyStaff.getDepartmentId());
        dto.setStaffId(dutyStaff.getStaffId());
        dto.setDisplayOrder(dutyStaff.getDisplayOrder());
        
        HisStaff staff = hisStaffRepository.findById(dutyStaff.getStaffId()).orElse(null);
        if (staff != null) {
            dto.setStaffCode(staff.getStaffCode());
            dto.setStaffName(staff.getName());
            dto.setTitle(staff.getTitle());
        }
        
        return dto;
    }
}