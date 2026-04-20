CREATE TABLE dept_patient_overview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code VARCHAR(50) NOT NULL,
    dept_id VARCHAR(50),
    total_num INT DEFAULT 0,
    new_in_hos INT DEFAULT 0,
    dise_num INT DEFAULT 0,
    death_num INT DEFAULT 0,
    out_num INT DEFAULT 0,
    surg_num INT DEFAULT 0,
    trans_in INT DEFAULT 0,
    trans_out INT DEFAULT 0,
    synced_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE INDEX idx_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;