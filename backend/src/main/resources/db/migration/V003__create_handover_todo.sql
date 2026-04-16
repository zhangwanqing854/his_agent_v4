CREATE TABLE handover_todo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    handover_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    due_time DATETIME NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (handover_id) REFERENCES shift_handover(id)
);