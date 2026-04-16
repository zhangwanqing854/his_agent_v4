-- 数据库更新脚本 - 添加同步相关字段
-- 执行前请备份数据库

-- 1. visit表新增字段
ALTER TABLE `visit` 
  ADD COLUMN `patient_no` VARCHAR(50) NULL COMMENT '患者编码',
  ADD COLUMN `patient_name` VARCHAR(50) NULL COMMENT '患者姓名',
  ADD COLUMN `patient_his_id` VARCHAR(50) NULL COMMENT '患者HIS主键',
  ADD COLUMN `dept_his_id` VARCHAR(50) NULL COMMENT '科室HIS主键',
  ADD COLUMN `visit_type` VARCHAR(20) NULL COMMENT '就诊类型',
  ADD COLUMN `admission_count` INT NULL COMMENT '住院次数',
  ADD COLUMN `nurse_area_id` VARCHAR(50) NULL COMMENT '病区主键',
  ADD COLUMN `nurse_area_code` VARCHAR(20) NULL COMMENT '病区编码',
  ADD COLUMN `nurse_area_name` VARCHAR(50) NULL COMMENT '病区名称';

-- 修改visit表部分字段的nullable
ALTER TABLE `visit` 
  MODIFY COLUMN `patient_id` BIGINT NULL,
  MODIFY COLUMN `dept_id` BIGINT NULL,
  MODIFY COLUMN `nurse_level` VARCHAR(20) NOT NULL DEFAULT '二级护理',
  MODIFY COLUMN `nurse_level_code` VARCHAR(10) DEFAULT '02',
  MODIFY COLUMN `patient_status` VARCHAR(20) NOT NULL DEFAULT '在院';

-- 2. order_main表新增字段
ALTER TABLE `order_main`
  ADD COLUMN `his_id` VARCHAR(50) NULL COMMENT 'HIS主键',
  ADD COLUMN `order_name` VARCHAR(200) NULL COMMENT '医嘱名称',
  ADD COLUMN `service_type` VARCHAR(20) NULL COMMENT '服务类型编码';

-- 修改order_main表部分字段的nullable
ALTER TABLE `order_main`
  MODIFY COLUMN `visit_id` BIGINT NULL,
  MODIFY COLUMN `patient_id` BIGINT NULL,
  MODIFY COLUMN `doctor_id` BIGINT NULL,
  MODIFY COLUMN `order_type` VARCHAR(20) NULL,
  MODIFY COLUMN `order_category` VARCHAR(50) NULL,
  MODIFY COLUMN `doctor_name` VARCHAR(50) NULL;

-- 3. diagnosis_main表修改nullable
ALTER TABLE `diagnosis_main`
  MODIFY COLUMN `visit_id` BIGINT NULL,
  MODIFY COLUMN `patient_id` BIGINT NULL;

-- 4. 确认order_item表有item_id字段（如果不存在则添加）
-- ALTER TABLE `order_item` ADD COLUMN `item_id` VARCHAR(50) NULL COMMENT '项目ID';

-- 完成提示
SELECT 'Database migration completed successfully!' AS message;