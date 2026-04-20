package com.hospital.handover.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.handover.service.VoskRecognizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.vosk.Recognizer;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "vosk.enabled", havingValue = "true")
public class VoiceWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(VoiceWebSocketHandler.class);
    
    private final VoskRecognizerService voskService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Recognizer> recognizers = new ConcurrentHashMap<>();
    
    public VoiceWebSocketHandler(VoskRecognizerService voskService) {
        this.voskService = voskService;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        Recognizer recognizer = voskService.createRecognizer();
        recognizers.put(session.getId(), recognizer);
        
        sendMessage(session, new VoiceMessage("connected", "Connection established", false));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        VoiceMessage msg = objectMapper.readValue(payload, VoiceMessage.class);
        
        Recognizer recognizer = recognizers.get(session.getId());
        if (recognizer == null) {
            sendMessage(session, new VoiceMessage("error", "Recognizer not found", false));
            return;
        }
        
        if ("audio".equals(msg.getType())) {
            byte[] audioData = Base64.getDecoder().decode(msg.getData());
            boolean accepted = recognizer.acceptWaveForm(audioData, audioData.length);
            
            if (accepted) {
                String resultJson = recognizer.getResult();
                VoiceResult result = objectMapper.readValue(resultJson, VoiceResult.class);
                sendMessage(session, new VoiceMessage("result", result.getText(), false));
            } else {
                String partialJson = recognizer.getPartialResult();
                VoicePartial partial = objectMapper.readValue(partialJson, VoicePartial.class);
                if (partial.getPartial() != null && !partial.getPartial().isEmpty()) {
                    sendMessage(session, new VoiceMessage("partial", partial.getPartial(), true));
                }
            }
        } else if ("end".equals(msg.getType())) {
            String finalResultJson = recognizer.getFinalResult();
            VoiceResult finalResult = objectMapper.readValue(finalResultJson, VoiceResult.class);
            sendMessage(session, new VoiceMessage("final", finalResult.getText(), false));
            
            recognizer.reset();
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {} - {}", session.getId(), status);
        
        Recognizer recognizer = recognizers.remove(session.getId());
        if (recognizer != null) {
            recognizer.close();
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error: {}", session.getId(), exception);
        sendMessage(session, new VoiceMessage("error", exception.getMessage(), false));
    }
    
    private void sendMessage(WebSocketSession session, VoiceMessage message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }
    
    public static class VoiceMessage {
        private String type;
        private String data;
        private String text;
        private boolean partial;
        
        public VoiceMessage() {}
        
        public VoiceMessage(String type, String text, boolean partial) {
            this.type = type;
            this.text = text;
            this.partial = partial;
        }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean getPartial() { return partial; }
        public void setPartial(boolean partial) { this.partial = partial; }
    }
    
    public static class VoiceResult {
        private String text;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
    
    public static class VoicePartial {
        private String partial;
        
        public String getPartial() { return partial; }
        public void setPartial(String partial) { this.partial = partial; }
    }
}