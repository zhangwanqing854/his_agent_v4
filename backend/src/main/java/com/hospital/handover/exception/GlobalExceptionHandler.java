package com.hospital.handover.exception;

import com.hospital.handover.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("全局异常: {}, 请求: {}", ex.getMessage(), request.getDescription(false), ex);
        
        String friendlyMessage = convertToFriendlyMessage(ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(500, friendlyMessage));
    }
    
    @ExceptionHandler(SyncException.class)
    public ResponseEntity<ApiResponse<Object>> handleSyncException(SyncException ex, WebRequest request) {
        logger.error("同步异常: {}, 请求: {}", ex.getMessage(), request.getDescription(false), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(500, ex.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("参数异常: {}, 请求: {}", ex.getMessage(), request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(400, "参数错误: " + ex.getMessage()));
    }
    
    private String convertToFriendlyMessage(Exception ex) {
        String message = ex.getMessage();
        
        if (message == null) {
            return "系统内部错误，请联系管理员";
        }
        
        if (message.contains("HIS接口连接超时") || message.contains("HIS接口请求超时")) {
            return "HIS接口响应超时，请稍后重试或检查网络连接";
        }
        
        if (message.contains("无法连接到HIS接口服务")) {
            return "HIS接口服务不可用，请确认接口地址正确且服务正在运行";
        }
        
        if (message.contains("HIS接口返回数据格式异常") || message.contains("HIS接口返回数据解析失败")) {
            return "HIS接口返回数据格式错误，无法解析业务数据";
        }
        
        if (message.contains("HIS接口返回数据为空")) {
            return "HIS接口返回数据为空，暂无同步数据";
        }
        
        return "操作失败: " + message;
    }
}