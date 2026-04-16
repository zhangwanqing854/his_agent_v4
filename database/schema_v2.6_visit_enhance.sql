-- 就诊信息表字段增强 v2.6
-- 新增字段：patient_no, patient_name, patient_his_id, dept_his_id, visit_type, admission_count, nurse_area_id, nurse_area_code, nurse_area_name

-- 1. 添加visit表缺失字段
ALTER TABLE visit 
ADD COLUMN IF NOT EXISTS patient_no VARCHAR(50) COMMENT '患者编码' AFTER patient_id,
ADD COLUMN IF NOT EXISTS patient_name VARCHAR(50) COMMENT '患者名称' AFTER patient_no,
ADD COLUMN IF NOT EXISTS patient_his_id VARCHAR(50) COMMENT '患者HIS主键' AFTER patient_name,
ADD COLUMN IF NOT EXISTS dept_his_id VARCHAR(50) COMMENT '科室HIS主键' AFTER dept_id,
ADD COLUMN IF NOT EXISTS visit_type VARCHAR(20) COMMENT '就诊类型(10住院,0103急诊留观)' AFTER bed_no,
ADD COLUMN IF NOT EXISTS admission_count INT DEFAULT 1 COMMENT '住院次数' AFTER discharge_datetime,
ADD COLUMN IF NOT EXISTS nurse_area_id VARCHAR(50) COMMENT '病区HIS主键' AFTER nurse_level_code,
ADD COLUMN IF NOT EXISTS nurse_area_code VARCHAR(20) COMMENT '病区编码' AFTER nurse_area_id,
ADD COLUMN IF NOT EXISTS nurse_area_name VARCHAR(50) COMMENT '病区名称' AFTER nurse_area_code;

-- 2. 添加就诊信息同步接口的字段映射（病区相关字段）
-- 查找HIS_VISIT_SYNC配置的mapping_table_id
SET @visit_mapping_id = (SELECT id FROM interface_mapping_table WHERE config_id = (SELECT id FROM interface_config WHERE config_code = 'HIS_VISIT_SYNC') LIMIT 1);

-- 插入病区字段映射
INSERT IGNORE INTO interface_field_mapping (mapping_table_id, source_field, target_field, transform_type, is_required, sort_order)
VALUES 
(@visit_mapping_id, 'id_dep_nur', 'nurse_area_id', 'DIRECT', 0, 10),
(@visit_mapping_id, 'code_dep_nur', 'nurse_area_code', 'DIRECT', 0, 11),
(@visit_mapping_id, 'name_dep_nur', 'nurse_area_name', 'DIRECT', 0, 12);

-- 3. 更新已有的字段映射（确保与HIS接口字段对应）
UPDATE interface_field_mapping 
SET target_field = 'patient_his_id'
WHERE mapping_table_id = @visit_mapping_id AND source_field = 'id_pat';

UPDATE interface_field_mapping 
SET target_field = 'dept_his_id'
WHERE mapping_table_id = @visit_mapping_id AND source_field = 'id_dep_phy';

UPDATE interface_field_mapping 
SET target_field = 'visit_type'
WHERE mapping_table_id = @visit_mapping_id AND source_field = 'code_entp';

UPDATE interface_field_mapping 
SET target_field = 'admission_count'
WHERE mapping_table_id = @visit_mapping_id AND source_field = 'times_ip';

-- 4. 添加索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_visit_patient_his_id ON visit(patient_his_id);
CREATE INDEX IF NOT EXISTS idx_visit_dept_his_id ON visit(dept_his_id);
CREATE INDEX IF NOT EXISTS idx_visit_nurse_area_code ON visit(nurse_area_code);

-- 验证更新
SELECT 'visit表字段更新完成' AS message;
SELECT COUNT(*) AS field_count FROM information_schema.columns WHERE table_schema = 'handover_system' AND table_name = 'visit';