package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Duty;
import com.hospital.handover.entity.Permission;
import com.hospital.handover.repository.DutyRepository;
import com.hospital.handover.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final DutyRepository dutyRepository;

    public PermissionService(PermissionRepository permissionRepository, 
                            DutyRepository dutyRepository) {
        this.permissionRepository = permissionRepository;
        this.dutyRepository = dutyRepository;
    }

    public List<PermissionDto> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        List<PermissionDto> result = new ArrayList<>();
        
        for (Permission perm : permissions) {
            result.add(toDto(perm));
        }
        
        return result;
    }

    public PermissionDto getPermissionById(Long id) {
        Optional<Permission> perm = permissionRepository.findById(id);
        return perm.map(this::toDto).orElse(null);
    }

    public List<DutyDto> getDutiesByPermissionId(Long permissionId) {
        List<Duty> duties = dutyRepository.findAll();
        List<DutyDto> result = new ArrayList<>();
        
        for (Duty duty : duties) {
            if (duty.getPermissionId().equals(permissionId)) {
                DutyDto dto = new DutyDto(duty.getId(), duty.getCode(), duty.getName());
                dto.setPermissionId(duty.getPermissionId());
                
                Optional<Permission> perm = permissionRepository.findById(duty.getPermissionId());
                perm.ifPresent(p -> dto.setPermissionName(p.getName()));
                
                result.add(dto);
            }
        }
        
        return result;
    }

    private PermissionDto toDto(Permission perm) {
        PermissionDto dto = new PermissionDto();
        dto.setId(perm.getId());
        dto.setCode(perm.getCode());
        dto.setName(perm.getName());
        dto.setDescription(perm.getDescription());
        return dto;
    }
}