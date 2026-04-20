CREATE TABLE vital_signs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_no VARCHAR(50) NOT NULL,
    sign_type VARCHAR(50) NOT NULL,
    sign_value VARCHAR(100),
    sign_unit VARCHAR(20),
    recorded_at DATETIME,
    synced_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_visit_no (visit_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;