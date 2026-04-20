package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.service.SystemConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
public class SystemConfigController {
    
    private final SystemConfigService systemConfigService;
    
    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllConfigs() {
        Map<String, String> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<String>> getConfig(@PathVariable String key) {
        String value = systemConfigService.getConfig(key);
        if (value == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "配置项不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(value));
    }
    
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateConfigs(@RequestBody Map<String, String> configs) {
        systemConfigService.updateConfigs(configs);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> updateConfig(
            @PathVariable String key,
            @RequestBody String value) {
        systemConfigService.updateConfig(key, value);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}