package com.hospital.handover.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.handover.config.HisSoapProperties;
import com.hospital.handover.util.SoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class HisSoapClient {
    
    private static final Logger logger = LoggerFactory.getLogger(HisSoapClient.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final HisSoapProperties properties;
    
    public HisSoapClient(HisSoapProperties properties) {
        this.properties = properties;
    }
    
    public JsonNode callGetEntVt(LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        return callWithRetry("getEntVt", startTime, endTime);
    }
    
    public JsonNode callGetDeptPatientInfo(String deptCode) throws Exception {
        return callWithRetry("getDeptPatientInfo", deptCode);
    }
    
    private JsonNode callWithRetry(String action, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        String url = properties.getFullUrl("");
        String namespace = properties.getNamespace();
        String params = SoapClient.buildTimeParams("arg0", "arg1", startTime, endTime, "yyyy-MM-dd HH:mm:ss");
        
        int maxRetries = 3;
        int retryInterval = 5000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("SOAP调用 {} (尝试 {}/{}): 时间范围 {} - {}", action, attempt, maxRetries, startTime, endTime);
                JsonNode result = SoapClient.callSoapService(url, action, namespace, params, properties.getTimeoutMs());
                logger.info("SOAP调用 {} 成功，返回数据条数: {}", action, result.isArray() ? result.size() : 1);
                return result;
            } catch (Exception e) {
                logger.warn("SOAP调用 {} 失败 (尝试 {}/{}): {}", action, attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("SOAP调用被中断", ie);
                    }
                } else {
                    throw new Exception("SOAP调用 " + action + " 失败，已重试" + maxRetries + "次: " + e.getMessage(), e);
                }
            }
        }
        
        throw new Exception("SOAP调用 " + action + " 失败");
    }
    
    private JsonNode callWithRetry(String action, String deptCode) throws Exception {
        String url = properties.getFullUrl("");
        String namespace = properties.getNamespace();
        
        StringBuilder paramsJson = new StringBuilder("[");
        paramsJson.append("{\"name\":\"arg0\",\"value\":\"").append(deptCode != null ? deptCode : "").append("\"}");
        paramsJson.append("]");
        String params = paramsJson.toString();
        
        int maxRetries = 3;
        int retryInterval = 5000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("SOAP调用 {} (尝试 {}/{}): 科室编码 {}", action, attempt, maxRetries, deptCode != null ? deptCode : "全部");
                JsonNode result = SoapClient.callSoapService(url, action, namespace, params, properties.getTimeoutMs());
                logger.info("SOAP调用 {} 成功，返回数据条数: {}", action, result.isArray() ? result.size() : 1);
                return result;
            } catch (Exception e) {
                logger.warn("SOAP调用 {} 失败 (尝试 {}/{}): {}", action, attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("SOAP调用被中断", ie);
                    }
                } else {
                    throw new Exception("SOAP调用 " + action + " 失败，已重试" + maxRetries + "次: " + e.getMessage(), e);
                }
            }
        }
        
        throw new Exception("SOAP调用 " + action + " 失败");
    }
}