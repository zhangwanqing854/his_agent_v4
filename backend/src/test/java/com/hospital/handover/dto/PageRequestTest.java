package com.hospital.handover.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PageRequestTest {

    @Test
    void testDefaultValues() {
        PageRequest request = new PageRequest();
        
        assertEquals(1, request.getPage());
        assertEquals(10, request.getSize());
    }

    @Test
    void testCustomValues() {
        PageRequest request = new PageRequest(3, 20);
        
        assertEquals(3, request.getPage());
        assertEquals(20, request.getSize());
    }

    @Test
    void testOffsetCalculation() {
        PageRequest request1 = new PageRequest(1, 10);
        assertEquals(0, request1.getOffset());
        
        PageRequest request2 = new PageRequest(2, 10);
        assertEquals(10, request2.getOffset());
        
        PageRequest request3 = new PageRequest(5, 20);
        assertEquals(80, request3.getOffset());
    }

    @Test
    void testSettersAndGetters() {
        PageRequest request = new PageRequest();
        request.setPage(5);
        request.setSize(50);
        
        assertEquals(5, request.getPage());
        assertEquals(50, request.getSize());
    }
}