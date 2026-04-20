package com.hospital.handover.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SOAP Client utility for calling HIS SOAP services and extracting JSON data from responses
 */
public class SoapClient {
    
    private static final Logger logger = LoggerFactory.getLogger(SoapClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern RETURN_PATTERN = Pattern.compile("<return[^>]*>(.*?)</return>", Pattern.DOTALL);
    
    /**
     * Call SOAP service and return parsed JSON data
     * 
     * @param url SOAP endpoint URL
     * @param soapAction SOAP action name (e.g., "getDept", "getEmp")
     * @param soapNamespace SOAP namespace (e.g., "http://i.sync.common.pkuih.iih/")
     * @param soapParams List of parameter name-value pairs (e.g., [{"name":"arg0","value":"2025-01-01"}])
     * @param timeoutMs Connection timeout in milliseconds
     * @return JsonNode containing the parsed response data
     */
    public static JsonNode callSoapService(String url, String soapAction, String soapNamespace, 
                                            String soapParams, int timeoutMs) throws Exception {
        String soapEnvelope = buildSoapEnvelope(soapAction, soapNamespace, soapParams);
        
        logger.info("SOAP Request to {}: {}", url, soapEnvelope);
        
        HttpURLConnection connection;
        try {
            connection = createConnection(url, timeoutMs);
        } catch (java.net.SocketTimeoutException e) {
            throw new Exception("HIS接口连接超时，请稍后重试或检查网络连接");
        } catch (java.net.ConnectException e) {
            throw new Exception("无法连接到HIS接口服务，请确认接口地址正确且服务正在运行");
        } catch (java.net.UnknownHostException e) {
            throw new Exception("HIS接口地址无法解析，请检查网络或接口配置");
        } catch (Exception e) {
            throw new Exception("HIS接口连接失败：" + e.getMessage());
        }
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(soapEnvelope.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (java.net.SocketTimeoutException e) {
            throw new Exception("HIS接口请求超时，数据量较大或网络不稳定");
        } catch (Exception e) {
            throw new Exception("HIS接口请求发送失败：" + e.getMessage());
        }
        
        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (Exception e) {
            throw new Exception("HIS接口无响应，请检查服务状态");
        }
        
        if (responseCode != 200) {
            String errorMessage = "";
            try {
                errorMessage = readErrorResponse(connection);
                logger.error("SOAP error response: {}", errorMessage);
            } catch (Exception e) {
                errorMessage = "无法读取错误详情";
            }
            
            String friendlyMessage;
            switch (responseCode) {
                case 400:
                    friendlyMessage = "HIS接口请求格式错误，请检查接口参数配置";
                    break;
                case 401:
                    friendlyMessage = "HIS接口认证失败，请检查访问权限配置";
                    break;
                case 403:
                    friendlyMessage = "HIS接口拒绝访问，请确认授权配置";
                    break;
                case 404:
                    friendlyMessage = "HIS接口地址不存在，请确认接口路径正确";
                    break;
                case 500:
                    friendlyMessage = "HIS接口内部错误，请联系管理员检查HIS服务";
                    break;
                case 503:
                    friendlyMessage = "HIS接口服务暂时不可用，请稍后重试";
                    break;
                default:
                    friendlyMessage = "HIS接口返回异常状态(" + responseCode + ")";
            }
            throw new Exception(friendlyMessage);
        }
        
        String response;
        try {
            response = readResponse(connection);
        } catch (Exception e) {
            throw new Exception("HIS接口响应数据读取失败：" + e.getMessage());
        }
        
        logger.info("SOAP Response: {}", response.length() > 500 ? response.substring(0, 500) + "..." : response);
        
        String jsonData;
        try {
            jsonData = extractJsonFromSoapResponse(response);
        } catch (Exception e) {
            throw new Exception("HIS接口返回数据格式异常，无法解析业务数据");
        }
        
        if (jsonData == null || jsonData.trim().isEmpty()) {
            logger.info("HIS接口返回空数据，本次同步无新增数据");
            return objectMapper.createArrayNode();
        }
        
        jsonData = decodeHtmlEntities(jsonData);
        
        JsonNode result;
        try {
            result = objectMapper.readTree(jsonData);
        } catch (Exception e) {
            logger.error("JSON parse error for data: {}", jsonData.length() > 200 ? jsonData.substring(0, 200) + "..." : jsonData);
            throw new Exception("HIS接口返回数据解析失败，数据格式不正确");
        }
        
        return result;
    }
    
    public static boolean testSoapConnection(String url, String soapAction, String soapNamespace, 
                                               String soapParams, int timeoutMs) {
        try {
            String soapEnvelope = buildSoapEnvelope(soapAction, soapNamespace, soapParams);
            logger.info("SOAP Test Request to {}: {}", url, soapEnvelope);
            
            HttpURLConnection connection;
            try {
                connection = createConnection(url, timeoutMs);
            } catch (java.net.SocketTimeoutException e) {
                throw new RuntimeException("HIS接口连接超时，请检查网络或接口地址");
            } catch (java.net.ConnectException e) {
                throw new RuntimeException("无法连接到HIS接口服务，请确认地址正确且服务运行");
            } catch (java.net.UnknownHostException e) {
                throw new RuntimeException("HIS接口地址无法解析，请检查接口配置");
            }
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(soapEnvelope.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (java.net.SocketTimeoutException e) {
                throw new RuntimeException("HIS接口请求超时，请稍后重试");
            }
            
            int responseCode = connection.getResponseCode();
            logger.info("SOAP Test Response code: {}", responseCode);
            
            if (responseCode == 200) {
                String response = readResponse(connection);
                logger.info("SOAP Test Response length: {}", response.length());
                return true;
            } else if (responseCode == 404) {
                throw new RuntimeException("HIS接口地址不存在，请确认接口路径");
            } else if (responseCode >= 500) {
                throw new RuntimeException("HIS接口服务异常，请检查HIS服务状态");
            } else {
                throw new RuntimeException("HIS接口返回异常状态(" + responseCode + ")");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("SOAP Test failed: {}", e.getMessage());
            throw new RuntimeException("HIS接口测试失败：" + e.getMessage());
        }
    }
    
    private static String decodeHtmlEntities(String text) {
        if (text == null) return null;
        return text.replace("&#xD;", "\r")
                   .replace("&#xA;", "\n")
                   .replace("&#x9;", "\t")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&amp;", "&")
                   .replace("&quot;", "\"")
                   .replace("&apos;", "'");
    }
    
    /**
     * Build SOAP envelope with parameters
     */
    private static String buildSoapEnvelope(String soapAction, String soapNamespace, String soapParams) {
        StringBuilder envelope = new StringBuilder();
        envelope.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        envelope.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        envelope.append("xmlns:i=\"").append(soapNamespace).append("\">");
        envelope.append("<soapenv:Header/>");
        envelope.append("<soapenv:Body>");
        envelope.append("<i:").append(soapAction).append(">");
        
        if (soapParams != null && !soapParams.isEmpty()) {
            try {
                JsonNode paramsNode = objectMapper.readTree(soapParams);
                if (paramsNode.isArray()) {
                    for (JsonNode param : paramsNode) {
                        String name = param.has("name") ? param.get("name").asText() : "";
                        String value = param.has("value") ? param.get("value").asText() : "";
                        envelope.append("<i:").append(name).append(">");
                        envelope.append(escapeXml(value));
                        envelope.append("</i:").append(name).append(">");
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to parse soapParams JSON: {}", soapParams);
            }
        }
        
        envelope.append("</i:").append(soapAction).append(">");
        envelope.append("</soapenv:Body>");
        envelope.append("</soapenv:Envelope>");
        
        return envelope.toString();
    }
    
    /**
     * Create HTTP connection for SOAP request
     */
    private static HttpURLConnection createConnection(String url, int timeoutMs) throws Exception {
        URL endpoint = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", "");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        
        return connection;
    }
    
    /**
     * Read response from successful connection
     */
    private static String readResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
    
    /**
     * Read error response from failed connection
     */
    private static String readErrorResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
    
    /**
     * Extract JSON data from SOAP response by parsing the <return> element
     */
    private static String extractJsonFromSoapResponse(String soapResponse) {
        Matcher matcher = RETURN_PATTERN.matcher(soapResponse);
        if (matcher.find()) {
            String returnContent = matcher.group(1);
            returnContent = returnContent.trim();
            if (returnContent.startsWith("<![CDATA[")) {
                returnContent = returnContent.substring(9, returnContent.length() - 3);
            }
            return returnContent;
        }
        logger.warn("SOAP响应中未找到<return>元素，返回空数据");
        return "";
    }
    
    /**
     * Escape XML special characters
     */
    private static String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    /**
     * Build SOAP parameters JSON from time range
     */
    public static String buildTimeParams(String paramStartName, String paramEndName, 
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          String timeFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
        
        StringBuilder paramsJson = new StringBuilder("[");
        paramsJson.append("{\"name\":\"").append(paramStartName).append("\",");
        paramsJson.append("\"value\":\"").append(startTime.format(formatter)).append("\"},");
        paramsJson.append("{\"name\":\"").append(paramEndName).append("\",");
        paramsJson.append("\"value\":\"").append(endTime.format(formatter)).append("\"}");
        paramsJson.append("]");
        
        return paramsJson.toString();
    }
}