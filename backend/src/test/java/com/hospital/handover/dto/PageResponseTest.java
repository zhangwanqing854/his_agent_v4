package com.hospital.handover.dto;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void testOf() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        PageResponse<String> response = PageResponse.of(content, 100, 2, 10);
        
        assertEquals(3, response.getContent().size());
        assertEquals(100, response.getTotalElements());
        assertEquals(10, response.getTotalPages());
        assertEquals(2, response.getPageNumber());
        assertEquals(10, response.getPageSize());
    }

    @Test
    void testTotalPagesCalculation() {
        List<String> content = Arrays.asList("item1");
        
        PageResponse<String> response1 = PageResponse.of(content, 10, 1, 10);
        assertEquals(1, response1.getTotalPages());
        
        PageResponse<String> response2 = PageResponse.of(content, 11, 1, 10);
        assertEquals(2, response2.getTotalPages());
        
        PageResponse<String> response3 = PageResponse.of(content, 25, 1, 10);
        assertEquals(3, response3.getTotalPages());
    }

    @Test
    void testSettersAndGetters() {
        PageResponse<Integer> response = new PageResponse<>();
        List<Integer> content = Arrays.asList(1, 2, 3);
        
        response.setContent(content);
        response.setTotalElements(50);
        response.setTotalPages(5);
        response.setPageNumber(3);
        response.setPageSize(10);
        
        assertEquals(content, response.getContent());
        assertEquals(50, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertEquals(3, response.getPageNumber());
        assertEquals(10, response.getPageSize());
    }
}