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
        
        HttpURLConnection connection = createConnection(url, timeoutMs);
        
        try (OutputStream os = connection.getOutputStream()) {
            os.write(soapEnvelope.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            String errorMessage = readErrorResponse(connection);
            throw new RuntimeException("SOAP request failed with code " + responseCode + ": " + errorMessage);
        }
        
        String response = readResponse(connection);
        logger.info("SOAP Response: {}", response.length() > 500 ? response.substring(0, 500) + "..." : response);
        
        String jsonData = extractJsonFromSoapResponse(response);
        jsonData = decodeHtmlEntities(jsonData);
        return objectMapper.readTree(jsonData);
    }
    
    public static boolean testSoapConnection(String url, String soapAction, String soapNamespace, 
                                               String soapParams, int timeoutMs) {
        try {
            String soapEnvelope = buildSoapEnvelope(soapAction, soapNamespace, soapParams);
            logger.info("SOAP Test Request to {}: {}", url, soapEnvelope);
            
            HttpURLConnection connection = createConnection(url, timeoutMs);
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(soapEnvelope.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            int responseCode = connection.getResponseCode();
            logger.info("SOAP Test Response code: {}", responseCode);
            
            if (responseCode == 200) {
                String response = readResponse(connection);
                logger.info("SOAP Test Response length: {}", response.length());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("SOAP Test failed: {}", e.getMessage());
            throw new RuntimeException("SOAP连接失败: " + e.getMessage(), e);
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
            // The HIS SOAP service returns JSON array directly in the <return> element
            // Clean up any CDATA or extra whitespace
            returnContent = returnContent.trim();
            if (returnContent.startsWith("<![CDATA[")) {
                returnContent = returnContent.substring(9, returnContent.length() - 3);
            }
            return returnContent;
        }
        throw new RuntimeException("Failed to extract JSON from SOAP response - no <return> element found");
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