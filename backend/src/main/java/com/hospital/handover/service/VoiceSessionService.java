package com.hospital.handover.service;

import com.hospital.handover.entity.VoiceSession;
import com.hospital.handover.repository.VoiceSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VoiceSessionService {

    private final VoiceSessionRepository voiceSessionRepository;

    public VoiceSessionService(VoiceSessionRepository voiceSessionRepository) {
        this.voiceSessionRepository = voiceSessionRepository;
    }

    public VoiceSession createSession(String sessionId) {
        VoiceSession session = new VoiceSession();
        session.setSessionId(sessionId);
        session.setStatus("pending");
        return voiceSessionRepository.save(session);
    }

    public Optional<VoiceSession> getSession(String sessionId) {
        return voiceSessionRepository.findBySessionId(sessionId);
    }

    @Transactional
    public VoiceSession updateTranscript(String sessionId, String transcript) {
        Optional<VoiceSession> optionalSession = voiceSessionRepository.findBySessionId(sessionId);
        if (optionalSession.isEmpty()) {
            VoiceSession session = new VoiceSession();
            session.setSessionId(sessionId);
            session.setTranscript(transcript);
            session.setStatus("completed");
            session.setUpdatedAt(LocalDateTime.now());
            return voiceSessionRepository.save(session);
        }

        VoiceSession session = optionalSession.get();
        session.setTranscript(transcript);
        session.setStatus("completed");
        session.setUpdatedAt(LocalDateTime.now());
        return voiceSessionRepository.save(session);
    }

    @Transactional
    public void markAsReceived(String sessionId) {
        Optional<VoiceSession> optionalSession = voiceSessionRepository.findBySessionId(sessionId);
        if (optionalSession.isPresent()) {
            VoiceSession session = optionalSession.get();
            session.setStatus("received");
            session.setUpdatedAt(LocalDateTime.now());
            voiceSessionRepository.save(session);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredSessions() {
        voiceSessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}