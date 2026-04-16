package com.hospital.handover.util;

import com.hospital.handover.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("handover-system-secret-key-very-long-for-security-2024");
        jwtProperties.setExpiration(86400000);
        jwtUtil = new JwtUtil(jwtProperties);
    }

    @Test
    void testGenerateTokenWithUserIdAndUsername() {
        String token = jwtUtil.generateToken(1L, "admin");
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testGenerateTokenWithDepartmentId() {
        String token = jwtUtil.generateToken(1L, "admin", 100L);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtUtil.generateToken(1L, "testuser");
        
        String username = jwtUtil.getUsernameFromToken(token);
        
        assertEquals("testuser", username);
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateToken(123L, "admin");
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        assertEquals(123L, userId);
    }

    @Test
    void testGetDepartmentIdFromToken() {
        String token = jwtUtil.generateToken(1L, "admin", 555L);
        
        Long deptId = jwtUtil.getDepartmentIdFromToken(token);
        
        assertEquals(555L, deptId);
    }

    @Test
    void testGetDepartmentIdFromTokenWhenNotPresent() {
        String token = jwtUtil.generateToken(1L, "admin");
        
        Long deptId = jwtUtil.getDepartmentIdFromToken(token);
        
        assertNull(deptId);
    }

    @Test
    void testValidateTokenValid() {
        String token = jwtUtil.generateToken(1L, "admin");
        
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateTokenInvalid() {
        String invalidToken = "invalid.token.here";
        
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testValidateTokenEmpty() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void testValidateTokenNull() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testIsTokenExpiredFalse() {
        String token = jwtUtil.generateToken(1L, "admin");
        
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpiredInvalidToken() {
        assertTrue(jwtUtil.isTokenExpired("invalid.token"));
    }

    @Test
    void testTokenContainsCorrectClaims() {
        Long userId = 999L;
        String username = "doctor1";
        Long deptId = 100L;
        
        String token = jwtUtil.generateToken(userId, username, deptId);
        
        assertEquals(userId, jwtUtil.getUserIdFromToken(token));
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
        assertEquals(deptId, jwtUtil.getDepartmentIdFromToken(token));
    }
}