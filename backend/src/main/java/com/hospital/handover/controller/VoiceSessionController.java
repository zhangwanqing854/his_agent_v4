package com.hospital.handover.controller;

import com.hospital.handover.dto.ApiResponse;
import com.hospital.handover.entity.VoiceSession;
import com.hospital.handover.service.VoiceSessionService;
import com.hospital.handover.service.XunfeiSpeechService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/voice-session")
public class VoiceSessionController {

    private final VoiceSessionService voiceSessionService;
    private final XunfeiSpeechService xunfeiSpeechService;

    public VoiceSessionController(VoiceSessionService voiceSessionService, 
                                   XunfeiSpeechService xunfeiSpeechService) {
        this.voiceSessionService = voiceSessionService;
        this.xunfeiSpeechService = xunfeiSpeechService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadAudio(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("sessionId") String sessionId) throws IOException {

        byte[] audioData = audio.getBytes();
        String transcript = xunfeiSpeechService.recognize(audioData);

        if (transcript != null && !transcript.isEmpty()) {
            voiceSessionService.updateTranscript(sessionId, transcript);
        }

        Map<String, Object> result = Map.of(
                "transcript", transcript != null ? transcript : "",
                "sessionId", sessionId
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VoiceSession>> createSession() {
        String sessionId = "VC" + System.currentTimeMillis() / 1000;
        VoiceSession session = voiceSessionService.createSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @PostMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<VoiceSession>> submitTranscript(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {

        String transcript = body.get("transcript");
        if (transcript == null || transcript.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(400, "语音内容不能为空"));
        }

        VoiceSession session = voiceSessionService.updateTranscript(sessionId, transcript);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<VoiceSession>> getSession(
            @PathVariable String sessionId) {

        VoiceSession session = voiceSessionService.getSession(sessionId)
                .orElse(null);

        if (session == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "会话不存在或已过期"));
        }

        if ("completed".equals(session.getStatus())) {
            voiceSessionService.markAsReceived(sessionId);
        }

        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus(
            @PathVariable String sessionId) {

        VoiceSession session = voiceSessionService.getSession(sessionId)
                .orElse(null);

        if (session == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "会话不存在或已过期"));
        }

        Map<String, Object> result = Map.of(
                "status", session.getStatus(),
                "transcript", session.getTranscript() != null ? session.getTranscript() : "",
                "hasContent", session.getTranscript() != null && !session.getTranscript().isEmpty()
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}