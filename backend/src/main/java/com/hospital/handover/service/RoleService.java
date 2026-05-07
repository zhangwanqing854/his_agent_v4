package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Duty;
import com.hospital.handover.entity.Role;
import com.hospital.handover.entity.RoleDuty;
import com.hospital.handover.repository.DutyRepository;
import com.hospital.handover.repository.RoleDutyRepository;
import com.hospital.handover.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final DutyRepository dutyRepository;
    private final RoleDutyRepository roleDutyRepository;

    public RoleService(RoleRepository roleRepository, 
                       DutyRepository dutyRepository,
                       RoleDutyRepository roleDutyRepository) {
        this.roleRepository = roleRepository;
        this.dutyRepository = dutyRepository;
        this.roleDutyRepository = roleDutyRepository;
    }

    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDto> result = new ArrayList<>();
        
        for (Role role : roles) {
            result.add(toDto(role));
        }
        
        return result;
    }

    public RoleDto getRoleById(Long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.map(this::toDto).orElse(null);
    }

    public RoleDto createRole(RoleCreateRequest request) {
        if (roleRepository.findByCode(request.getCode()) != null) {
            throw new RuntimeException("角色编码已存在");
        }
        
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        role.setDescription(request.getDescription());
        
        Role saved = roleRepository.save(role);
        
        if (request.getDutyIds() != null && !request.getDutyIds().isEmpty()) {
            for (Long dutyId : request.getDutyIds()) {
                RoleDuty roleDuty = new RoleDuty();
                roleDuty.setRoleId(saved.getId());
                roleDuty.setDutyId(dutyId);
                roleDutyRepository.save(roleDuty);
            }
        }
        
        return toDto(saved);
    }

    public RoleDto updateRole(Long id, RoleUpdateRequest request) {
        Optional<Role> optional = roleRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("角色不存在");
        }
        
        Role role = optional.get();
        
        if (role.getCode().equals("SUPER_ADMIN")) {
            throw new RuntimeException("无法修改超级管理员角色");
        }
        
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            Role existing = roleRepository.findByCode(request.getCode());
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("角色编码已存在");
            }
            role.setCode(request.getCode());
        }
        
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        
        if (request.getIsDefault() != null) {
            role.setIsDefault(request.getIsDefault());
        }
        
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        
        Role saved = roleRepository.save(role);
        
        if (request.getDutyIds() != null) {
            List<RoleDuty> existingDuties = roleDutyRepository.findAll().stream()
                .filter(rd -> rd.getRoleId().equals(id))
                .collect(Collectors.toList());
            
            for (RoleDuty rd : existingDuties) {
                roleDutyRepository.delete(rd);
            }
            
            for (Long dutyId : request.getDutyIds()) {
                RoleDuty roleDuty = new RoleDuty();
                roleDuty.setRoleId(saved.getId());
                roleDuty.setDutyId(dutyId);
                roleDutyRepository.save(roleDuty);
            }
        }
        
        return toDto(saved);
    }

    public void deleteRole(Long id) {
        Optional<Role> optional = roleRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("角色不存在");
        }
        
        Role role = optional.get();
        
        if (role.getCode().equals("SUPER_ADMIN") || role.getCode().equals("DOCTOR")) {
            throw new RuntimeException("无法删除预置角色");
        }
        
        roleRepository.deleteById(id);
    }

    public List<DutyDto> getDutiesByRoleId(Long roleId) {
        List<RoleDuty> roleDuties = roleDutyRepository.findAll().stream()
            .filter(rd -> rd.getRoleId().equals(roleId))
            .collect(Collectors.toList());
        
        List<DutyDto> result = new ArrayList<>();
        for (RoleDuty rd : roleDuties) {
            Optional<Duty> duty = dutyRepository.findById(rd.getDutyId());
            duty.ifPresent(d -> result.add(new DutyDto(d.getId(), d.getCode(), d.getName(), d.getDescription())));
        }
        
        return result;
    }

    private RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setIsDefault(role.getIsDefault());
        dto.setDescription(role.getDescription());
        
        List<RoleDuty> roleDuties = roleDutyRepository.findAll().stream()
            .filter(rd -> rd.getRoleId().equals(role.getId()))
            .collect(Collectors.toList());
        
        List<Long> dutyIds = roleDuties.stream()
            .map(RoleDuty::getDutyId)
            .collect(Collectors.toList());
        
        dto.setDutyIds(dutyIds);
        
        List<DutyDto> duties = new ArrayList<>();
        for (RoleDuty rd : roleDuties) {
            Optional<Duty> duty = dutyRepository.findById(rd.getDutyId());
            duty.ifPresent(d -> {
                DutyDto dutyDto = new DutyDto();
                dutyDto.setId(d.getId());
                dutyDto.setCode(d.getCode());
                dutyDto.setName(d.getName());
                dutyDto.setDescription(d.getDescription());
                duties.add(dutyDto);
            });
        }
        dto.setDuties(duties);
        
        return dto;
    }
}