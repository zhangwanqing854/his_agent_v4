package com.hospital.handover.config;

import com.hospital.handover.websocket.VoiceWebSocketHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@ConditionalOnProperty(name = "vosk.enabled", havingValue = "true")
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final VoiceWebSocketHandler voiceWebSocketHandler;
    
    public WebSocketConfig(VoiceWebSocketHandler voiceWebSocketHandler) {
        this.voiceWebSocketHandler = voiceWebSocketHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceWebSocketHandler, "/ws/voice")
                .setAllowedOrigins("*");
    }
}