-- ============================================================
-- 数据库变更记录 V2.3 - 补充编码字段
-- 变更内容：
--   1. transfer_record 表新增 visit_no 字段
--   2. diagnosis_main 表新增 visit_no、patient_no 字段
-- 变更原因：
--   HIS接口通过编码关联数据，这两张表缺少编码字段无法同步
-- ============================================================

-- -----------------------------------------------------------
-- 1. transfer_record 表新增字段
-- -----------------------------------------------------------
ALTER TABLE `transfer_record`
  ADD COLUMN `visit_no` VARCHAR(50) NOT NULL COMMENT '就诊编码（HIS的code_ent）' AFTER `visit_id`,
  ADD COLUMN `patient_no` VARCHAR(50) DEFAULT NULL COMMENT '患者编码（HIS的code_pat）' AFTER `patient_id`,
  ADD KEY `idx_visit_no` (`visit_no`);

-- -----------------------------------------------------------
-- 2. diagnosis_main 表新增字段
-- -----------------------------------------------------------
ALTER TABLE `diagnosis_main`
  ADD COLUMN `visit_no` VARCHAR(50) NOT NULL COMMENT '就诊编码（HIS的code_ent）' AFTER `visit_id`,
  ADD COLUMN `patient_no` VARCHAR(50) NOT NULL COMMENT '患者编码（HIS的code_pat）' AFTER `patient_id`,
  ADD KEY `idx_visit_no` (`visit_no`);

-- ============================================================
-- 变更结束
-- ============================================================