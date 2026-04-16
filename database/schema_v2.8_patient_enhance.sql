-- ============================================================
-- Schema v2.8: Patient 表新增字段
-- 新增：sd_sex(性别编码)、sd_nation(民族编码)、name_nation(民族名称)、sd_country(国家编码)、name_country(国家名称)
-- ============================================================

-- 新增性别编码字段
ALTER TABLE patient ADD COLUMN sd_sex VARCHAR(10) DEFAULT NULL COMMENT '性别编码' AFTER gender;

-- 新增民族相关字段
ALTER TABLE patient ADD COLUMN sd_nation VARCHAR(10) DEFAULT NULL COMMENT '民族编码' AFTER sd_sex;
ALTER TABLE patient ADD COLUMN name_nation VARCHAR(50) DEFAULT NULL COMMENT '民族名称' AFTER sd_nation;

-- 新增国家相关字段
ALTER TABLE patient ADD COLUMN sd_country VARCHAR(10) DEFAULT NULL COMMENT '国家编码' AFTER name_nation;
ALTER TABLE patient ADD COLUMN name_country VARCHAR(50) DEFAULT NULL COMMENT '国家名称' AFTER sd_country;

-- 添加索引
ALTER TABLE patient ADD INDEX idx_sd_nation (sd_nation);
ALTER TABLE patient ADD INDEX idx_sd_country (sd_country);

-- 修改 name 字段允许为空（HIS数据可能缺少姓名）
ALTER TABLE patient MODIFY COLUMN name VARCHAR(50) NULL COMMENT '姓名';