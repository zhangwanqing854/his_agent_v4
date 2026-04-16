-- ============================================================
-- 数据库变更脚本 V2.4
-- 日期：2026-04-08
-- 变更内容：
--   1. visit 表增加 nurse_level_code 护理等级编码字段
--   2. order_main 表增加 visit_no 和 patient_no 字段
--   3. order_main 表 order_type 字段值改为 Y/N 格式
-- ============================================================

SET NAMES utf8mb4;

-- 1. visit 表增加护理等级编码
ALTER TABLE `visit` 
ADD COLUMN `nurse_level_code` VARCHAR(10) DEFAULT NULL COMMENT '护理等级编码（00特级/01一级/02二级/03三级）' AFTER `nurse_level`;

-- 添加索引
ALTER TABLE `visit` 
ADD INDEX `idx_nurse_level_code` (`nurse_level_code`);

-- 2. order_main 表增加就诊编码和患者编码
ALTER TABLE `order_main`
ADD COLUMN `visit_no` VARCHAR(50) DEFAULT NULL COMMENT '就诊编码（HIS的code_ent）' AFTER `visit_id`,
ADD COLUMN `patient_no` VARCHAR(50) DEFAULT NULL COMMENT '患者编码（HIS的code_pat）' AFTER `patient_id`;

-- 添加索引
ALTER TABLE `order_main`
ADD INDEX `idx_visit_no` (`visit_no`),
ADD INDEX `idx_patient_no` (`patient_no`);

-- 3. 修改 order_type 字段注释，说明 Y=长期 N=临时
ALTER TABLE `order_main`
MODIFY COLUMN `order_type` VARCHAR(1) NOT NULL COMMENT '医嘱类型（Y=长期医嘱/N=临时医嘱）';

-- ============================================================
-- 同步已有数据（如果有数据的话）
-- ============================================================

-- 从 visit 表同步 visit_no 到 order_main
UPDATE `order_main` om
INNER JOIN `visit` v ON om.visit_id = v.id
SET om.visit_no = v.visit_no
WHERE om.visit_no IS NULL;

-- 从 visit 表关联 patient 表同步 patient_no
UPDATE `order_main` om
INNER JOIN `visit` v ON om.visit_id = v.id
INNER JOIN `patient` p ON v.patient_id = p.id
SET om.patient_no = p.patient_no
WHERE om.patient_no IS NULL;

SET FOREIGN_KEY_CHECKS = 1;