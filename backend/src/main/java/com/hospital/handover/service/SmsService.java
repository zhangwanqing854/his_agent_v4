package com.hospital.handover.service;

import com.hospital.handover.dto.SmsResult;

public interface SmsService {
    
    SmsResult send(String phone, String content);
}