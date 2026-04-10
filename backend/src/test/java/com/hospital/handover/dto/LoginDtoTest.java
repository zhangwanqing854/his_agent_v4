package com.hospital.handover.dto;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LoginDtoTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testLoginRequestValid() {
        LoginRequest request = new LoginRequest("admin", "password123");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLoginRequestUsernameBlank() {
        LoginRequest request = new LoginRequest();
        request.setUsercode("");
        request.setPassword("password");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户编码")));
    }

    @Test
    void testLoginRequestPasswordBlank() {
        LoginRequest request = new LoginRequest();
        request.setUsercode("admin");
        request.setPassword("");
        
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码")));
    }

    @Test
    void testLoginRequestWithCaptcha() {
        LoginRequest request = new LoginRequest("admin", "password");
        request.setCaptcha("1234");
        request.setCaptchaId("captcha-123");
        
        assertEquals("1234", request.getCaptcha());
        assertEquals("captcha-123", request.getCaptchaId());
    }

    @Test
    void testLoginResponse() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("admin");
        userInfo.setName("张医生");
        
        LoginResponse response = new LoginResponse("token-abc", userInfo);
        
        assertEquals("token-abc", response.getToken());
        assertEquals(1L, response.getUserInfo().getId());
        assertEquals("admin", response.getUserInfo().getUsername());
    }

    @Test
    void testUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("admin");
        userInfo.setName("张医生");
        userInfo.setRole("doctor");
        userInfo.setAvatar("/avatar.png");
        
        DepartmentInfo dept1 = new DepartmentInfo(1L, "XNK", "心内科", true);
        DepartmentInfo dept2 = new DepartmentInfo(2L, "SJNK", "神经内科", false);
        userInfo.setDepartments(Arrays.asList(dept1, dept2));
        
        assertEquals(1L, userInfo.getId());
        assertEquals("admin", userInfo.getUsername());
        assertEquals("张医生", userInfo.getName());
        assertEquals("doctor", userInfo.getRole());
        assertEquals(2, userInfo.getDepartments().size());
    }

    @Test
    void testDepartmentInfo() {
        DepartmentInfo dept = new DepartmentInfo(1L, "XNK", "心内科", true);
        
        assertEquals(1L, dept.getId());
        assertEquals("XNK", dept.getCode());
        assertEquals("心内科", dept.getName());
        assertTrue(dept.getIsPrimary());
    }

    @Test
    void testDepartmentInfoSetters() {
        DepartmentInfo dept = new DepartmentInfo();
        dept.setId(2L);
        dept.setCode("SJNK");
        dept.setName("神经内科");
        dept.setIsPrimary(false);
        
        assertEquals(2L, dept.getId());
        assertEquals("SJNK", dept.getCode());
        assertEquals("神经内科", dept.getName());
        assertFalse(dept.getIsPrimary());
    }
}