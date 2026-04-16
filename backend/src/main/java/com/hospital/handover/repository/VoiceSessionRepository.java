package com.hospital.handover.repository;

import com.hospital.handover.entity.VoiceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceSessionRepository extends JpaRepository<VoiceSession, Long> {

    Optional<VoiceSession> findBySessionId(String sessionId);

    List<VoiceSession> findByStatusAndExpiresAtAfter(String status, LocalDateTime now);

    void deleteByExpiresAtBefore(LocalDateTime now);
}