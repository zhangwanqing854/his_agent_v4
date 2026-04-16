-- Create scheduling table (排班主表)
CREATE TABLE IF NOT EXISTS scheduling (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    `year_month` VARCHAR(7) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'draft',
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_department_year_month (department_id, `year_month`),
    CONSTRAINT fk_scheduling_department FOREIGN KEY (department_id) REFERENCES department(id),
    CONSTRAINT fk_scheduling_created_by FOREIGN KEY (created_by) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create scheduling_detail table (排班明细表)
CREATE TABLE IF NOT EXISTS scheduling_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheduling_id BIGINT NOT NULL,
    duty_date DATE NOT NULL,
    staff_id BIGINT,
    remark VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_scheduling_date (scheduling_id, duty_date),
    CONSTRAINT fk_scheduling_detail_scheduling FOREIGN KEY (scheduling_id) REFERENCES scheduling(id) ON DELETE CASCADE,
    CONSTRAINT fk_scheduling_detail_staff FOREIGN KEY (staff_id) REFERENCES his_staff(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create department_scheduling_config table (科室排班配置表)
CREATE TABLE IF NOT EXISTS department_scheduling_config (
    department_id BIGINT PRIMARY KEY,
    staff_order JSON NOT NULL,
    last_position INT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_scheduling_config_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;