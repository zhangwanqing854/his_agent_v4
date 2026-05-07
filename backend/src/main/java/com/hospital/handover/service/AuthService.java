package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.DoctorDepartment;
import com.hospital.handover.entity.Duty;
import com.hospital.handover.entity.Role;
import com.hospital.handover.entity.RoleDuty;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.DutyRepository;
import com.hospital.handover.repository.RoleDutyRepository;
import com.hospital.handover.repository.RoleRepository;
import com.hospital.handover.repository.UserRepository;
import com.hospital.handover.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorDepartmentRepository doctorDepartmentRepository;
    private final DutyRepository dutyRepository;
    private final RoleDutyRepository roleDutyRepository;
    private final JwtUtil jwtUtil;
    private final SyncService syncService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       DepartmentRepository departmentRepository,
                       DoctorDepartmentRepository doctorDepartmentRepository,
                       DutyRepository dutyRepository,
                       RoleDutyRepository roleDutyRepository,
                       JwtUtil jwtUtil,
                       SyncService syncService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.doctorDepartmentRepository = doctorDepartmentRepository;
        this.dutyRepository = dutyRepository;
        this.roleDutyRepository = roleDutyRepository;
        this.jwtUtil = jwtUtil;
        this.syncService = syncService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsercodeIgnoreCase(request.getUsercode());
        
        if (user == null) {
            throw new RuntimeException("用户编码或密码错误");
        }
        
        if (!user.getEnabled()) {
            throw new RuntimeException("账号已被禁用");
        }
        
        if (!verifyPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户编码或密码错误");
        }
        
        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        String roleCode = role != null ? role.getCode() : "UNKNOWN";
        
        List<DepartmentInfo> departments = getUserDepartments(user.getId());
        List<DutyDto> duties = getUserDuties(user.getRoleId(), user.getIsSuperAdmin());
        
        Long primaryDeptId = departments.stream()
                .filter(DepartmentInfo::getIsPrimary)
                .findFirst()
                .map(DepartmentInfo::getId)
                .orElse(null);
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), primaryDeptId);
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setHisStaffId(user.getHisStaffId());
        userInfo.setUsername(user.getUsername());
        userInfo.setUsercode(user.getUsercode());
        userInfo.setName(getUserName(user));
        userInfo.setRole(roleCode.toLowerCase());
        userInfo.setAvatar("");
        userInfo.setIsSuperAdmin(user.getIsSuperAdmin());
        userInfo.setDepartments(departments);
        userInfo.setDuties(duties);
        
        // 登录成功后触发数据同步（异步执行，不阻塞登录响应）
        String deptCode = departments.stream()
                .filter(DepartmentInfo::getIsPrimary)
                .findFirst()
                .map(DepartmentInfo::getCode)
                .orElse(null);
        
        if (deptCode != null) {
            try {
                syncService.executeBatchSync(deptCode, "LOGIN");
            } catch (Exception e) {
                // 同步失败不影响登录
            }
        }
        
        return new LoginResponse(token, userInfo);
    }

    public UserInfo getCurrentUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        String roleCode = role != null ? role.getCode() : "UNKNOWN";
        
        List<DepartmentInfo> departments = getUserDepartments(user.getId());
        List<DutyDto> duties = getUserDuties(user.getRoleId(), user.getIsSuperAdmin());
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setHisStaffId(user.getHisStaffId());
        userInfo.setUsername(user.getUsername());
        userInfo.setUsercode(user.getUsercode());
        userInfo.setName(getUserName(user));
        userInfo.setRole(roleCode.toLowerCase());
        userInfo.setAvatar("");
        userInfo.setIsSuperAdmin(user.getIsSuperAdmin());
        userInfo.setDepartments(departments);
        userInfo.setDuties(duties);
        
        return userInfo;
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword) || 
               encodedPassword.contains(rawPassword);
    }

    private String getUserName(User user) {
        return user.getUsername();
    }

    private List<DepartmentInfo> getUserDepartments(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }
        
        if (Boolean.TRUE.equals(user.getIsSuperAdmin())) {
            List<Department> allDepartments = departmentRepository.findAll();
            List<DepartmentInfo> departments = new ArrayList<>();
            
            for (Department dept : allDepartments) {
                departments.add(new DepartmentInfo(
                    dept.getId(),
                    dept.getCode(),
                    dept.getName(),
                    false
                ));
            }
            
            if (!departments.isEmpty()) {
                departments.get(0).setIsPrimary(true);
            }
            
            return departments;
        }
        
        List<DoctorDepartment> doctorDepts = doctorDepartmentRepository.findAll();
        List<DepartmentInfo> departments = new ArrayList<>();
        
        for (DoctorDepartment dd : doctorDepts) {
            if (dd.getDoctorId().equals(userId)) {
                Department dept = departmentRepository.findById(dd.getDepartmentId()).orElse(null);
                if (dept != null) {
                    departments.add(new DepartmentInfo(
                        dept.getId(),
                        dept.getCode(),
                        dept.getName(),
                        dd.getIsPrimary()
                    ));
                }
            }
        }
        
        // 如果没有主科室，自动设置第一个科室为主科室
        if (!departments.isEmpty()) {
            boolean hasPrimary = departments.stream().anyMatch(DepartmentInfo::getIsPrimary);
            if (!hasPrimary) {
                departments.get(0).setIsPrimary(true);
            }
        }
        
        return departments;
    }
    
    private List<DutyDto> getUserDuties(Long roleId, Boolean isSuperAdmin) {
        if (Boolean.TRUE.equals(isSuperAdmin)) {
            List<Duty> allDuties = dutyRepository.findAll();
            return allDuties.stream()
                .map(d -> new DutyDto(d.getId(), d.getCode(), d.getName(), d.getDescription()))
                .collect(Collectors.toList());
        }
        
        if (roleId == null) {
            return new ArrayList<>();
        }
        
        List<RoleDuty> roleDuties = roleDutyRepository.findByRoleId(roleId);
        List<Long> dutyIds = roleDuties.stream()
            .map(RoleDuty::getDutyId)
            .collect(Collectors.toList());
        
        if (dutyIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Duty> duties = dutyRepository.findAllById(dutyIds);
        return duties.stream()
            .map(d -> new DutyDto(d.getId(), d.getCode(), d.getName(), d.getDescription()))
            .collect(Collectors.toList());
    }
}