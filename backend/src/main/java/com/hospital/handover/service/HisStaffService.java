package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.DoctorDepartment;
import com.hospital.handover.entity.HisStaff;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.HisStaffRepository;
import com.hospital.handover.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HisStaffService {

    private final HisStaffRepository hisStaffRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DoctorDepartmentRepository doctorDepartmentRepository;

    public HisStaffService(HisStaffRepository hisStaffRepository,
                          DepartmentRepository departmentRepository,
                          UserRepository userRepository,
                          DoctorDepartmentRepository doctorDepartmentRepository) {
        this.hisStaffRepository = hisStaffRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.doctorDepartmentRepository = doctorDepartmentRepository;
    }

    public List<HisStaffDto> getAllStaff() {
        List<HisStaff> staffList = hisStaffRepository.findAll();
        return toDtoList(staffList);
    }

    public List<HisStaffDto> getStaffByDepartment(Long departmentId) {
        if (departmentId == null) {
            return new ArrayList<>();
        }
        
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
        
        List<HisStaff> staffList = hisStaffRepository.findAllById(hisStaffIds);
        return toDtoListWithDepartment(staffList, departmentId);
    }

    public HisStaffDto getStaffById(Long id) {
        Optional<HisStaff> staff = hisStaffRepository.findById(id);
        return staff.map(this::toDto).orElse(null);
    }

    public List<HisStaffDto> getUnlinkedStaff() {
        List<HisStaff> allStaff = hisStaffRepository.findAll();
        
        List<User> users = userRepository.findAll();
        List<Long> linkedStaffIds = users.stream()
            .filter(u -> u.getHisStaffId() != null)
            .map(User::getHisStaffId)
            .collect(Collectors.toList());
        
        List<HisStaff> unlinkedStaff = allStaff.stream()
            .filter(s -> !linkedStaffIds.contains(s.getId()))
            .collect(Collectors.toList());
        
        return toDtoList(unlinkedStaff);
    }

    public HisStaffDto createStaff(HisStaffCreateRequest request) {
        if (hisStaffRepository.findByStaffCode(request.getStaffCode()) != null) {
            throw new RuntimeException("员工编码已存在");
        }
        
        HisStaff staff = new HisStaff();
        staff.setStaffCode(request.getStaffCode());
        staff.setName(request.getName());
        staff.setDepartmentId(request.getDepartmentId());
        staff.setTitle(request.getTitle());
        staff.setPhone(request.getPhone());
        staff.setSyncTime(LocalDateTime.now());
        
        HisStaff saved = hisStaffRepository.save(staff);
        return toDto(saved);
    }

    public HisStaffDto updateStaff(Long id, HisStaffUpdateRequest request) {
        Optional<HisStaff> optional = hisStaffRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("员工不存在");
        }
        
        HisStaff staff = optional.get();
        
        if (request.getName() != null) {
            staff.setName(request.getName());
        }
        if (request.getDepartmentId() != null) {
            staff.setDepartmentId(request.getDepartmentId());
        }
        if (request.getTitle() != null) {
            staff.setTitle(request.getTitle());
        }
        if (request.getPhone() != null) {
            staff.setPhone(request.getPhone());
        }
        
        HisStaff saved = hisStaffRepository.save(staff);
        return toDto(saved);
    }

    public void deleteStaff(Long id) {
        Optional<HisStaff> optional = hisStaffRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("员工不存在");
        }
        
        List<User> linkedUsers = userRepository.findAll().stream()
            .filter(u -> id.equals(u.getHisStaffId()))
            .collect(Collectors.toList());
        
        if (!linkedUsers.isEmpty()) {
            throw new RuntimeException("该员工已关联用户账号，无法删除");
        }
        
        hisStaffRepository.deleteById(id);
    }

    private List<HisStaffDto> toDtoList(List<HisStaff> staffList) {
        List<HisStaffDto> result = new ArrayList<>();
        
        for (HisStaff staff : staffList) {
            result.add(toDto(staff));
        }
        
        return result;
    }

    private List<HisStaffDto> toDtoListWithDepartment(List<HisStaff> staffList, Long departmentId) {
        List<HisStaffDto> result = new ArrayList<>();
        Department department = departmentRepository.findById(departmentId).orElse(null);
        
        for (HisStaff staff : staffList) {
            HisStaffDto dto = toDto(staff);
            dto.setDepartmentId(departmentId);
            if (department != null) {
                dto.setDepartmentName(department.getName());
            }
            result.add(dto);
        }
        
        return result;
    }

    private HisStaffDto toDto(HisStaff staff) {
        HisStaffDto dto = new HisStaffDto();
        dto.setId(staff.getId());
        dto.setStaffCode(staff.getStaffCode());
        dto.setName(staff.getName());
        dto.setDepartmentId(staff.getDepartmentId());
        dto.setTitle(staff.getTitle());
        dto.setPhone(staff.getPhone());
        
        if (staff.getSyncTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setSyncTime(staff.getSyncTime().format(formatter));
        }
        
        if (staff.getDepartmentId() != null) {
            Optional<Department> dept = departmentRepository.findById(staff.getDepartmentId());
            dept.ifPresent(d -> dto.setDepartmentName(d.getName()));
        }
        
        return dto;
    }
}