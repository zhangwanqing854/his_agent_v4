-- 添加在院标识字段到 visit 表
-- fg_ip: 'Y' = 在院, 'N' = 不在院

ALTER TABLE visit ADD COLUMN fg_ip VARCHAR(1) DEFAULT 'Y';

-- 更新现有数据：出院的患者设置为不在院
UPDATE visit SET fg_ip = 'N' WHERE discharge_datetime IS NOT NULL AND discharge_datetime <= NOW();

-- 添加索引以提高查询性能
CREATE INDEX idx_visit_fg_ip ON visit(fg_ip);