package com.hospital.handover.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.handover.dto.*;
import com.hospital.handover.service.InterfaceConfigService;
import com.hospital.handover.util.SoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/interface-configs")
public class InterfaceConfigController {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceConfigController.class);
    private static final int TEST_TIMEOUT_MS = 30000;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final InterfaceConfigService configService;

    public InterfaceConfigController(InterfaceConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterfaceConfigDto>>> getAllConfigs() {
        List<InterfaceConfigDto> configs = configService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterfaceConfigDto>> getConfigById(@PathVariable Long id) {
        InterfaceConfigDto config = configService.getConfigById(id);
        if (config == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "接口配置不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InterfaceConfigDto>> createConfig(@RequestBody InterfaceConfigDto dto) {
        try {
            InterfaceConfigDto config = configService.createConfig(dto);
            return ResponseEntity.ok(ApiResponse.success("创建成功", config));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InterfaceConfigDto>> updateConfig(
            @PathVariable Long id,
            @RequestBody InterfaceConfigDto dto) {
        try {
            InterfaceConfigDto config = configService.updateConfig(id, dto);
            return ResponseEntity.ok(ApiResponse.success("更新成功", config));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        try {
            configService.deleteConfig(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<TestResultDto>> testConnection(@PathVariable Long id) {
        InterfaceConfigDto config = configService.getConfigById(id);
        if (config == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "接口配置不存在"));
        }
        
        TestResultDto result = testInterface(config);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping("/test-sync")
    public ResponseEntity<ApiResponse<SyncTestResultDto>> testSync(@RequestBody InterfaceConfigDto config) {
        SyncTestResultDto result = testSyncInterface(config);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    private SyncTestResultDto testSyncInterface(InterfaceConfigDto config) {
        SyncTestResultDto result = new SyncTestResultDto();
        long startTime = System.currentTimeMillis();
        
        try {
            if (config.getUrl() == null || config.getUrl().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("配置错误：URL为空");
                result.setResponseTime(0L);
                return result;
            }
            
            List<Object> data;
            if ("SOAP".equals(config.getApiProtocol())) {
                data = fetchSoapData(config);
            } else {
                data = fetchRestData(config);
            }
            
            int total = data.size();
            List<Object> preview = data.size() > 5 ? data.subList(0, 5) : data;
            
            result.setSuccess(true);
            result.setMessage("同步测试成功，获取 " + total + " 条数据");
            result.setDataCount(total);
            result.setDataPreview(new ArrayList<>(preview));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("同步测试失败: " + e.getMessage());
            result.setErrorDetail(e.getMessage());
            logger.error("Sync test failed: {}", e.getMessage(), e);
        } finally {
            result.setResponseTime(System.currentTimeMillis() - startTime);
        }
        
        return result;
    }
    
    private List<Object> fetchSoapData(InterfaceConfigDto config) throws Exception {
        String soapParams = buildSyncParams(config);
        
        JsonNode data = SoapClient.callSoapService(
            config.getUrl(),
            config.getSoapAction() != null ? config.getSoapAction() : "test",
            config.getSoapNamespace() != null ? config.getSoapNamespace() : "http://tempuri.org/",
            soapParams,
            TEST_TIMEOUT_MS
        );
        
        List<Object> result = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                result.add(objectMapper.treeToValue(node, Object.class));
            }
        } else {
            result.add(objectMapper.treeToValue(data, Object.class));
        }
        
        return result;
    }
    
    private List<Object> fetchRestData(InterfaceConfigDto config) throws Exception {
        URL url = new URL(config.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        String method = config.getMethod();
        if (method == null || method.isEmpty()) {
            method = "GET";
        }
        
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TEST_TIMEOUT_MS);
        connection.setReadTimeout(TEST_TIMEOUT_MS);
        connection.setDoInput(true);
        
        connection.setRequestProperty("Content-Type", "application/json");
        
        if (config.getAuthType() != null && !"NONE".equals(config.getAuthType()) && config.getAuthConfig() != null) {
            applyAuthHeaders(connection, config.getAuthType(), config.getAuthConfig());
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException("HTTP " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();
        
        JsonNode data = objectMapper.readTree(response.toString());
        
        String dataPath = null;
        if (config.getMappingTables() != null && !config.getMappingTables().isEmpty()) {
            dataPath = config.getMappingTables().get(0).getDataPath();
        }
        
        if (dataPath != null && !dataPath.isEmpty()) {
            data = getDataByPath(data, dataPath);
        }
        
        List<Object> result = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode node : data) {
                result.add(objectMapper.treeToValue(node, Object.class));
            }
        } else {
            result.add(objectMapper.treeToValue(data, Object.class));
        }
        
        return result;
    }
    
    private String buildSyncParams(InterfaceConfigDto config) {
        String soapParams = config.getSoapParams();
        if (soapParams == null || soapParams.isEmpty()) {
            soapParams = "[]";
        }
        
        try {
            JsonNode paramsNode = objectMapper.readTree(soapParams);
            String startParam = config.getSyncTimeParamStart() != null ? config.getSyncTimeParamStart() : "arg0";
            String endParam = config.getSyncTimeParamEnd() != null ? config.getSyncTimeParamEnd() : "arg1";
            String timeFormat = config.getSyncTimeFormat() != null ? config.getSyncTimeFormat() : "yyyy-MM-dd HH:mm:ss";
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime;
            int firstSyncDays = config.getFirstSyncDays() != null ? config.getFirstSyncDays() : 30;
            startTime = now.minusDays(firstSyncDays);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
            String startTimeStr = startTime.format(formatter);
            String endTimeStr = now.format(formatter);
            
            boolean hasStartParam = false;
            boolean hasEndParam = false;
            
            if (paramsNode.isArray()) {
                for (JsonNode param : paramsNode) {
                    String name = param.has("name") ? param.get("name").asText() : "";
                    if (name.equals(startParam)) hasStartParam = true;
                    if (name.equals(endParam)) hasEndParam = true;
                }
            }
            
            StringBuilder paramsBuilder = new StringBuilder("[");
            if (paramsNode.isArray()) {
                for (int i = 0; i < paramsNode.size(); i++) {
                    JsonNode param = paramsNode.get(i);
                    String name = param.has("name") ? param.get("name").asText() : "";
                    String value = param.has("value") ? param.get("value").asText() : "";
                    
                    if (name.equals(startParam)) {
                        value = startTimeStr;
                    } else if (name.equals(endParam)) {
                        value = endTimeStr;
                    }
                    
                    if (i > 0) paramsBuilder.append(",");
                    paramsBuilder.append("{\"name\":\"").append(name).append("\",\"value\":\"").append(value).append("\"}");
                }
            }
            
            if (!hasStartParam) {
                if (paramsBuilder.length() > 1) paramsBuilder.append(",");
                paramsBuilder.append("{\"name\":\"").append(startParam).append("\",\"value\":\"").append(startTimeStr).append("\"}");
            }
            if (!hasEndParam) {
                paramsBuilder.append(",{\"name\":\"").append(endParam).append("\",\"value\":\"").append(endTimeStr).append("\"}");
            }
            
            paramsBuilder.append("]");
            return paramsBuilder.toString();
            
        } catch (Exception e) {
            logger.warn("Failed to build sync params: {}", e.getMessage());
            return soapParams;
        }
    }
    
    private void applyAuthHeaders(HttpURLConnection connection, String authType, String authConfig) {
        try {
            JsonNode authNode = objectMapper.readTree(authConfig);
            
            if ("BEARER".equals(authType) && authNode.has("token")) {
                connection.setRequestProperty("Authorization", "Bearer " + authNode.get("token").asText());
            } else if ("BASIC".equals(authType) && authNode.has("username") && authNode.has("password")) {
                String credentials = java.util.Base64.getEncoder().encodeToString(
                    (authNode.get("username").asText() + ":" + authNode.get("password").asText()).getBytes()
                );
                connection.setRequestProperty("Authorization", "Basic " + credentials);
            } else if ("API_KEY".equals(authType) && authNode.has("apiKey")) {
                connection.setRequestProperty("X-API-Key", authNode.get("apiKey").asText());
            }
        } catch (Exception e) {
            logger.warn("Failed to parse auth config: {}", authConfig);
        }
    }
    
    private JsonNode getDataByPath(JsonNode data, String path) {
        if (path == null || path.isEmpty()) {
            return data;
        }
        
        String[] parts = path.split("\\.");
        JsonNode result = data;
        
        for (String part : parts) {
            if (result != null && result.has(part)) {
                result = result.get(part);
            } else {
                return data;
            }
        }
        
        return result;
    }
    
    private TestResultDto testInterface(InterfaceConfigDto config) {
        TestResultDto result = new TestResultDto();
        long startTime = System.currentTimeMillis();
        
        try {
            if (config.getUrl() == null || config.getUrl().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("配置错误：URL为空");
                result.setResponseTime(0L);
                return result;
            }
            
            if ("SOAP".equals(config.getApiProtocol())) {
                testSoapInterface(config, result);
            } else {
                testRestInterface(config, result);
            }
            
        } catch (java.net.SocketTimeoutException e) {
            result.setSuccess(false);
            result.setMessage("连接超时");
        } catch (java.net.ConnectException e) {
            result.setSuccess(false);
            result.setMessage("连接被拒绝");
        } catch (java.net.UnknownHostException e) {
            result.setSuccess(false);
            result.setMessage("主机不存在");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("测试失败: " + e.getMessage());
            logger.error("Interface test failed: {}", e.getMessage());
        } finally {
            result.setResponseTime(System.currentTimeMillis() - startTime);
        }
        
        return result;
    }
    
    private void testSoapInterface(InterfaceConfigDto config, TestResultDto result) {
        String soapParams = config.getSoapParams();
        if (soapParams == null || soapParams.isEmpty()) {
            soapParams = "[]";
        }
        
        try {
            boolean success = SoapClient.testSoapConnection(
                config.getUrl(),
                config.getSoapAction() != null ? config.getSoapAction() : "test",
                config.getSoapNamespace() != null ? config.getSoapNamespace() : "http://tempuri.org/",
                soapParams,
                TEST_TIMEOUT_MS
            );
            
            if (success) {
                result.setSuccess(true);
                result.setMessage("SOAP接口连接成功");
                result.setStatusCode(200);
            } else {
                result.setSuccess(false);
                result.setMessage("SOAP接口返回非200状态");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("SOAP接口测试失败: " + e.getMessage());
        }
    }
    
    private void testRestInterface(InterfaceConfigDto config, TestResultDto result) throws Exception {
        URL url = new URL(config.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        String method = config.getMethod();
        if (method == null || method.isEmpty()) {
            method = "GET";
        }
        
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TEST_TIMEOUT_MS);
        connection.setReadTimeout(TEST_TIMEOUT_MS);
        connection.setDoInput(true);
        
        int responseCode = connection.getResponseCode();
        result.setStatusCode(responseCode);
        
        if (responseCode >= 200 && responseCode < 300) {
            result.setSuccess(true);
            result.setMessage("REST接口连接成功 (HTTP " + responseCode + ")");
        } else if (responseCode == 401 || responseCode == 403) {
            result.setSuccess(false);
            result.setMessage("认证失败 (HTTP " + responseCode + ")");
        } else if (responseCode >= 500) {
            result.setSuccess(false);
            result.setMessage("服务器错误 (HTTP " + responseCode + ")");
        } else {
            result.setSuccess(false);
            result.setMessage("请求失败 (HTTP " + responseCode + ")");
        }
        
        connection.disconnect();
    }
}