-- 科室值班人员配置表
-- 用于存储科室参与排班的值班人员名单
-- Version: 2.12
-- Date: 2026-04-13

CREATE TABLE IF NOT EXISTS department_duty_staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    staff_id BIGINT NOT NULL COMMENT '人员ID（关联 his_staff.id）',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_dept_staff (department_id, staff_id),
    INDEX idx_department_id (department_id),
    INDEX idx_staff_id (staff_id),
    INDEX idx_display_order (department_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室值班人员配置表';