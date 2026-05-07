package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Role;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.RoleRepository;
import com.hospital.handover.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserDto> getUserList(UserFilterRequest filter) {
        List<User> users = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();
        
        for (User user : users) {
            if (filter != null) {
                if (filter.getUsername() != null && !filter.getUsername().isEmpty()) {
                    if (!user.getUsername().contains(filter.getUsername())) {
                        continue;
                    }
                }
                if (filter.getRoleId() != null) {
                    if (!filter.getRoleId().equals(user.getRoleId())) {
                        continue;
                    }
                }
                if (filter.getEnabled() != null) {
                    if (!filter.getEnabled().equals(user.getEnabled())) {
                        continue;
                    }
                }
            }
            result.add(toDto(user));
        }
        
        return result;
    }

    public UserDto getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(this::toDto).orElse(null);
    }

    public UserDto createUser(UserCreateRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setIsSuperAdmin(false);
        user.setHisStaffId(request.getHisStaffId());
        user.setRoleId(request.getRoleId());
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto updateUser(Long id, UserUpdateRequest request) {
        return updateUserWithOperator(id, request, null);
    }
    
    public UserDto updateUserWithOperator(Long id, UserUpdateRequest request, Long operatorId) {
        Optional<User> optional = userRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = optional.get();
        
        if (request.getIsSuperAdmin() != null) {
            if (operatorId == null) {
                throw new RuntimeException("修改超级管理员状态需要操作者身份");
            }
            
            Optional<User> operatorOpt = userRepository.findById(operatorId);
            if (operatorOpt.isEmpty() || !operatorOpt.get().getIsSuperAdmin()) {
                throw new RuntimeException("只有超级管理员可以修改其他用户的超管状态");
            }
            
            if (operatorId.equals(id)) {
                throw new RuntimeException("不能修改自己的超级管理员状态");
            }
            
            if (!request.getIsSuperAdmin() && user.getIsSuperAdmin()) {
                long superAdminCount = userRepository.countByIsSuperAdmin(true);
                if (superAdminCount <= 1) {
                    throw new RuntimeException("不能移除最后一个超级管理员");
                }
            }
            
            user.setIsSuperAdmin(request.getIsSuperAdmin());
        }
        
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            User existing = userRepository.findByUsername(request.getUsername());
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }
        
        if (request.getRoleId() != null) {
            user.setRoleId(request.getRoleId());
        }
        
        if (request.getHisStaffId() != null) {
            user.setHisStaffId(request.getHisStaffId());
        }
        
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public void deleteUser(Long id) {
        Optional<User> optional = userRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = optional.get();
        if (user.getIsSuperAdmin()) {
            throw new RuntimeException("无法删除超级管理员账号");
        }
        
        userRepository.deleteById(id);
    }

    public UserDto enableUser(Long id) {
        Optional<User> optional = userRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = optional.get();
        user.setEnabled(true);
        
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto disableUser(Long id) {
        Optional<User> optional = userRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = optional.get();
        if (user.getIsSuperAdmin()) {
            throw new RuntimeException("无法禁用超级管理员账号");
        }
        
        user.setEnabled(false);
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto resetPassword(Long id, String newPassword) {
        Optional<User> optional = userRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = optional.get();
        user.setPassword(newPassword);
        
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setUsercode(user.getUsercode());
        dto.setIsSuperAdmin(user.getIsSuperAdmin());
        dto.setHisStaffId(user.getHisStaffId());
        dto.setRoleId(user.getRoleId());
        dto.setEnabled(user.getEnabled());
        
        Optional<Role> role = roleRepository.findById(user.getRoleId());
        role.ifPresent(r -> {
            dto.setRoleName(r.getName());
            dto.setName(r.getName());
        });
        
        return dto;
    }
}