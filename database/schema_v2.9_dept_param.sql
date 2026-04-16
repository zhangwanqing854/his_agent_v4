-- =====================================================
-- 数据库变更脚本 v2.9
-- 功能: interface_config 表新增 dept_param 字段
-- 作者: Sisyphus
-- 日期: 2026-04-10
-- =====================================================

-- 新增 dept_param 字段，标识科室参数名
ALTER TABLE interface_config 
ADD COLUMN dept_param VARCHAR(20) DEFAULT NULL COMMENT '科室参数名，如 arg0，NULL 表示不需要科室参数' 
AFTER sync_time_param_end;

-- 更新诊断同步配置：arg0 是科室编码
UPDATE interface_config 
SET dept_param = 'arg0'
WHERE config_code = 'HIS_DIAGNOSIS_SYNC';

-- 更新医嘱同步配置：arg0 是科室编码，arg1/arg2 是时间参数
UPDATE interface_config 
SET dept_param = 'arg0',
    sync_time_param_start = 'arg1',
    sync_time_param_end = 'arg2'
WHERE config_code = 'HIS_ORDER_SYNC';

-- 验证更新结果
SELECT config_code, config_name, dept_param, sync_time_param_start, sync_time_param_end 
FROM interface_config 
WHERE dept_param IS NOT NULL;