package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.DoctorDepartment;
import com.hospital.handover.entity.Role;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.RoleRepository;
import com.hospital.handover.repository.UserRepository;
import com.hospital.handover.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorDepartmentRepository doctorDepartmentRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       DepartmentRepository departmentRepository,
                       DoctorDepartmentRepository doctorDepartmentRepository,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.doctorDepartmentRepository = doctorDepartmentRepository;
        this.jwtUtil = jwtUtil;
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
        userInfo.setDepartments(departments);
        
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
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setHisStaffId(user.getHisStaffId());
        userInfo.setUsername(user.getUsername());
        userInfo.setUsercode(user.getUsercode());
        userInfo.setName(getUserName(user));
        userInfo.setRole(roleCode.toLowerCase());
        userInfo.setAvatar("");
        userInfo.setDepartments(departments);
        
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
        
        return departments;
    }
}