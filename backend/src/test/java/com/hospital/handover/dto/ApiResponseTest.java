package com.hospital.handover.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        String data = "test data";
        ApiResponse<String> response = ApiResponse.success(data);
        
        assertEquals(0, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testSuccessWithMessage() {
        String data = "test data";
        ApiResponse<String> response = ApiResponse.success("操作成功", data);
        
        assertEquals(0, response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testErrorWithCode() {
        ApiResponse<String> response = ApiResponse.error(401, "未授权");
        
        assertEquals(401, response.getCode());
        assertEquals("未授权", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testErrorWithMessage() {
        ApiResponse<String> response = ApiResponse.error("操作失败");
        
        assertEquals(1, response.getCode());
        assertEquals("操作失败", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testSettersAndGetters() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("OK");
        response.setData(123);
        
        assertEquals(200, response.getCode());
        assertEquals("OK", response.getMessage());
        assertEquals(123, response.getData());
    }
}