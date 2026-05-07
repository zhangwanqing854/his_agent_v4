-- ============================================================
-- 数据库表描述补充脚本
-- 用途：为缺少 COMMENT 的表添加描述信息
-- 日期：2026-05-06
-- ============================================================

-- 排班主表
ALTER TABLE scheduling COMMENT '排班主表，记录科室每月的排班计划';

-- 排班明细表
ALTER TABLE scheduling_detail COMMENT '排班明细表，记录每天的具体值班人员安排';

-- 科室排班配置表
ALTER TABLE department_scheduling_config COMMENT '科室排班配置表，存储排班人员顺序和上次排班位置';

-- 交班待办事项表
ALTER TABLE handover_todo COMMENT '交班待办事项表，记录交班相关的待办任务';

-- 科室患者概览表
ALTER TABLE dept_patient_overview COMMENT '科室患者概览表，存储科室患者统计数据（总人数、危重、新入院等）';

-- 生命体征表
ALTER TABLE vital_signs COMMENT '生命体征表，存储患者体温、血压、心率等生命体征数据';

-- 系统配置表
ALTER TABLE system_config COMMENT '系统配置表，存储系统级配置参数（如报告URL、功能开关等）';

-- 语音会话表
ALTER TABLE voice_session COMMENT '语音会话表，记录语音识别会话状态和转录结果';

-- ============================================================
-- 执行完成
-- ============================================================
