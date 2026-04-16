-- 语音会话表
CREATE TABLE IF NOT EXISTS voice_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL UNIQUE,
    transcript TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    expires_at DATETIME NOT NULL
);

-- 添加索引
CREATE INDEX idx_voice_session_session_id ON voice_session(session_id);
CREATE INDEX idx_voice_session_status ON voice_session(status);
CREATE INDEX idx_voice_session_expires_at ON voice_session(expires_at);