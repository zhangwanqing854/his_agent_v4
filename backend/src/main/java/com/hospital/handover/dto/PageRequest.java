package com.hospital.handover.dto;

public class PageRequest {
    
    private int page = 1;
    private int size = 10;
    
    public PageRequest() {}
    
    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public int getOffset() {
        return (page - 1) * size;
    }
}