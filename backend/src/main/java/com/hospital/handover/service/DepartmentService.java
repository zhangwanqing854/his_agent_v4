package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentDto> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDto> result = new ArrayList<>();
        
        for (Department dept : departments) {
            result.add(toDto(dept));
        }
        
        return result;
    }

    public DepartmentDto getDepartmentById(Long id) {
        Optional<Department> dept = departmentRepository.findById(id);
        return dept.map(this::toDto).orElse(null);
    }

    public DepartmentDto getDepartmentByCode(String code) {
        Optional<Department> dept = departmentRepository.findByCode(code);
        return dept.map(this::toDto).orElse(null);
    }

    public DepartmentDto createDepartment(DepartmentCreateRequest request) {
        if (departmentRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("科室编码已存在");
        }
        
        Department dept = new Department();
        dept.setCode(request.getCode());
        dept.setName(request.getName());
        dept.setBedCount(request.getBedCount() != null ? request.getBedCount() : 0);
        
        Department saved = departmentRepository.save(dept);
        return toDto(saved);
    }

    public DepartmentDto updateDepartment(Long id, DepartmentUpdateRequest request) {
        Optional<Department> optional = departmentRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("科室不存在");
        }
        
        Department dept = optional.get();
        
        if (request.getName() != null) {
            dept.setName(request.getName());
        }
        if (request.getBedCount() != null) {
            dept.setBedCount(request.getBedCount());
        }
        
        Department saved = departmentRepository.save(dept);
        return toDto(saved);
    }

    public void deleteDepartment(Long id) {
        Optional<Department> optional = departmentRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("科室不存在");
        }
        
        departmentRepository.deleteById(id);
    }

    private DepartmentDto toDto(Department dept) {
        return new DepartmentDto(
            dept.getId(),
            dept.getCode(),
            dept.getName(),
            dept.getBedCount()
        );
    }
}