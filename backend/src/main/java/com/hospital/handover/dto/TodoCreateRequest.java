package com.hospital.handover.dto;

import java.time.LocalDateTime;

public class TodoCreateRequest {
    private String content;
    private LocalDateTime dueTime;

    public TodoCreateRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDueTime() { return dueTime; }
    public void setDueTime(LocalDateTime dueTime) { this.dueTime = dueTime; }
}