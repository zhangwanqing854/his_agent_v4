-- 数据库变更脚本 v2.10
-- 功能: interface_config 表新增 sync_order 字段，控制定时同步执行顺序

ALTER TABLE interface_config 
ADD COLUMN sync_order INT DEFAULT NULL COMMENT '定时同步执行顺序，NULL表示不参与定时同步' 
AFTER sync_schedule;

-- 更新需要定时同步的接口配置
UPDATE interface_config SET sync_schedule = '0 0 7 * * ?', sync_order = 1 WHERE config_code = 'HIS_DEPT_SYNC';
UPDATE interface_config SET sync_schedule = '0 0 7 * * ?', sync_order = 2 WHERE config_code = 'HIS_STAFF_SYNC';
UPDATE interface_config SET sync_schedule = '0 0 7 * * ?', sync_order = 3 WHERE config_code = 'HIS_DOCTOR_DEPT_SYNC';
UPDATE interface_config SET sync_schedule = '0 0 7 * * ?', sync_order = 4 WHERE config_code = 'HIS_PATIENT_SYNC';
UPDATE interface_config SET sync_schedule = '0 0 7 * * ?', sync_order = 5 WHERE config_code = 'HIS_VISIT_SYNC';
UPDATE interface_config SET sync_schedule = '0 0 7 * * ?', sync_order = 6 WHERE config_code = 'HIS_TRANSFER_SYNC';

-- 验证配置
SELECT config_code, config_name, sync_schedule, sync_order 
FROM interface_config 
WHERE sync_order IS NOT NULL 
ORDER BY sync_order;
