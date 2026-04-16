package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.dto.SmsConfigResponse;
import com.hospital.handover.dto.SmsConfigUpdateRequest;
import com.hospital.handover.dto.SmsResult;
import com.hospital.handover.service.SmsConfigService;
import com.hospital.handover.service.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms-config")
public class SmsConfigController {
    
    private final SmsConfigService smsConfigService;
    private final SmsService smsService;
    
    public SmsConfigController(SmsConfigService smsConfigService, SmsService smsService) {
        this.smsConfigService = smsConfigService;
        this.smsService = smsService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<SmsConfigResponse>> getConfigs() {
        SmsConfigResponse response = smsConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateConfigs(@RequestBody SmsConfigUpdateRequest request) {
        smsConfigService.updateConfigs(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Void>> testSend(@RequestParam String phone) {
        String testContent = "【北京大学国际医院】这是一条测试短信，用于验证短信配置是否正确。";
        SmsResult result = smsService.send(phone, testContent);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(null));
        } else {
            return ResponseEntity.ok(ApiResponse.error(500, "短信发送失败: " + result.getErrorMessage()));
        }
    }
}