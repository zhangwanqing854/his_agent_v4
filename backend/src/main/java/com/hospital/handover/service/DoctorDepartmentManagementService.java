package com.hospital.handover.service;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.DoctorDepartmentManagementDto;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.DoctorDepartment;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DoctorDepartmentManagementService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorDepartmentManagementService.class);

    private final DoctorDepartmentRepository doctorDepartmentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public DoctorDepartmentManagementService(DoctorDepartmentRepository doctorDepartmentRepository,
                                               UserRepository userRepository,
                                               DepartmentRepository departmentRepository) {
        this.doctorDepartmentRepository = doctorDepartmentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public ApiResponse<Map<String, Object>> getList(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<DoctorDepartment> ddPage;
        if (search != null && !search.trim().isEmpty()) {
            ddPage = doctorDepartmentRepository.findBySearch(search.trim(), pageable);
        } else {
            ddPage = doctorDepartmentRepository.findAll(pageable);
        }
        
        List<DoctorDepartmentManagementDto> dtoList = convertToDtoList(ddPage.getContent());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", dtoList);
        result.put("total", ddPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        
        return ApiResponse.success(result);
    }

    private List<DoctorDepartmentManagementDto> convertToDtoList(List<DoctorDepartment> ddList) {
        Map<Long, User> userMap = new HashMap<>();
        Map<Long, Department> deptMap = new HashMap<>();
        
        for (DoctorDepartment dd : ddList) {
            if (!userMap.containsKey(dd.getDoctorId())) {
                userRepository.findById(dd.getDoctorId()).ifPresent(u -> userMap.put(dd.getDoctorId(), u));
            }
            if (!deptMap.containsKey(dd.getDepartmentId())) {
                departmentRepository.findById(dd.getDepartmentId()).ifPresent(d -> deptMap.put(dd.getDepartmentId(), d));
            }
        }
        
        List<DoctorDepartmentManagementDto> dtoList = new java.util.ArrayList<>();
        for (DoctorDepartment dd : ddList) {
            DoctorDepartmentManagementDto dto = new DoctorDepartmentManagementDto();
            dto.setId(dd.getId());
            dto.setDoctorId(dd.getDoctorId());
            dto.setDepartmentId(dd.getDepartmentId());
            dto.setIsPrimary(dd.getIsPrimary());
            dto.setCreatedAt(dd.getCreatedAt());
            
            User user = userMap.get(dd.getDoctorId());
            if (user != null) {
                dto.setDoctorName(user.getUsername());
                dto.setDoctorUsercode(user.getUsercode());
            }
            
            Department dept = deptMap.get(dd.getDepartmentId());
            if (dept != null) {
                dto.setDepartmentCode(dept.getCode());
                dto.setDepartmentName(dept.getName());
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }

    @Transactional
    public ApiResponse<DoctorDepartmentManagementDto> create(Long doctorId, Long departmentId, Boolean isPrimary) {
        Optional<DoctorDepartment> existing = doctorDepartmentRepository.findByDoctorIdAndDepartmentId(doctorId, departmentId);
        if (existing.isPresent()) {
            return ApiResponse.error("该关系已存在");
        }
        
        Optional<User> userOpt = userRepository.findById(doctorId);
        if (!userOpt.isPresent()) {
            return ApiResponse.error("医护人员不存在");
        }
        
        Optional<Department> deptOpt = departmentRepository.findById(departmentId);
        if (!deptOpt.isPresent()) {
            return ApiResponse.error("科室不存在");
        }
        
        if (isPrimary != null && isPrimary) {
            List<DoctorDepartment> existingPrimary = doctorDepartmentRepository.findByDoctorId(doctorId);
            for (DoctorDepartment dd : existingPrimary) {
                if (dd.getIsPrimary()) {
                    dd.setIsPrimary(false);
                    doctorDepartmentRepository.save(dd);
                }
            }
        }
        
        DoctorDepartment dd = new DoctorDepartment();
        dd.setDoctorId(doctorId);
        dd.setDepartmentId(departmentId);
        dd.setIsPrimary(isPrimary != null ? isPrimary : false);
        doctorDepartmentRepository.save(dd);
        
        DoctorDepartmentManagementDto dto = new DoctorDepartmentManagementDto();
        dto.setId(dd.getId());
        dto.setDoctorId(doctorId);
        dto.setDoctorName(userOpt.get().getUsername());
        dto.setDoctorUsercode(userOpt.get().getUsercode());
        dto.setDepartmentId(departmentId);
        dto.setDepartmentCode(deptOpt.get().getCode());
        dto.setDepartmentName(deptOpt.get().getName());
        dto.setIsPrimary(dd.getIsPrimary());
        dto.setCreatedAt(dd.getCreatedAt());
        
        logger.info("Created doctor-department relation: doctor={}, dept={}", doctorId, departmentId);
        return ApiResponse.success("创建成功", dto);
    }

    @Transactional
    public ApiResponse<DoctorDepartmentManagementDto> update(Long id, Boolean isPrimary) {
        Optional<DoctorDepartment> ddOpt = doctorDepartmentRepository.findById(id);
        if (!ddOpt.isPresent()) {
            return ApiResponse.error("关系不存在");
        }
        
        DoctorDepartment dd = ddOpt.get();
        
        if (isPrimary != null && isPrimary && !dd.getIsPrimary()) {
            List<DoctorDepartment> existingPrimary = doctorDepartmentRepository.findByDoctorId(dd.getDoctorId());
            for (DoctorDepartment other : existingPrimary) {
                if (other.getIsPrimary() && !other.getId().equals(id)) {
                    other.setIsPrimary(false);
                    doctorDepartmentRepository.save(other);
                }
            }
        }
        
        dd.setIsPrimary(isPrimary != null ? isPrimary : dd.getIsPrimary());
        doctorDepartmentRepository.save(dd);
        
        DoctorDepartmentManagementDto dto = convertToDto(dd);
        logger.info("Updated doctor-department relation: id={}, isPrimary={}", id, isPrimary);
        return ApiResponse.success("更新成功", dto);
    }

    @Transactional
    public ApiResponse<Void> delete(Long id) {
        Optional<DoctorDepartment> ddOpt = doctorDepartmentRepository.findById(id);
        if (!ddOpt.isPresent()) {
            return ApiResponse.error("关系不存在");
        }
        
        doctorDepartmentRepository.deleteById(id);
        logger.info("Deleted doctor-department relation: id={}", id);
        return ApiResponse.success(null);
    }

    @Transactional
    public ApiResponse<Void> setPrimary(Long doctorId, Long departmentId) {
        // 查找该医生在该科室的关系
        Optional<DoctorDepartment> ddOpt = doctorDepartmentRepository.findByDoctorIdAndDepartmentId(doctorId, departmentId);
        if (!ddOpt.isPresent()) {
            return ApiResponse.error("该医生与该科室无关系，请先在科室人员管理中添加关系");
        }
        
        // 取消该医生其他科室的主科室标记
        List<DoctorDepartment> existingPrimary = doctorDepartmentRepository.findByDoctorId(doctorId);
        for (DoctorDepartment dd : existingPrimary) {
            if (dd.getIsPrimary() && !dd.getId().equals(ddOpt.get().getId())) {
                dd.setIsPrimary(false);
                doctorDepartmentRepository.save(dd);
            }
        }
        
        // 设置当前科室为主科室
        DoctorDepartment dd = ddOpt.get();
        dd.setIsPrimary(true);
        doctorDepartmentRepository.save(dd);
        
        logger.info("Set primary department: doctor={}, dept={}", doctorId, departmentId);
        return ApiResponse.success(null);
    }

    private DoctorDepartmentManagementDto convertToDto(DoctorDepartment dd) {
        DoctorDepartmentManagementDto dto = new DoctorDepartmentManagementDto();
        dto.setId(dd.getId());
        dto.setDoctorId(dd.getDoctorId());
        dto.setDepartmentId(dd.getDepartmentId());
        dto.setIsPrimary(dd.getIsPrimary());
        dto.setCreatedAt(dd.getCreatedAt());
        
        userRepository.findById(dd.getDoctorId()).ifPresent(u -> {
            dto.setDoctorName(u.getUsername());
            dto.setDoctorUsercode(u.getUsercode());
        });
        
        departmentRepository.findById(dd.getDepartmentId()).ifPresent(d -> {
            dto.setDepartmentCode(d.getCode());
            dto.setDepartmentName(d.getName());
        });
        
        return dto;
    }
}