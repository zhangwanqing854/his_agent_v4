-- 添加出生日期字段到 patient 表
-- birth_date: 患者出生日期

ALTER TABLE patient ADD COLUMN birth_date DATETIME NULL;