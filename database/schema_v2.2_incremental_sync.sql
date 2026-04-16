-- ============================================================
-- 数据库变更记录 V2.2 - 增量同步支持
-- 变更内容：
--   1. interface_config 表增加同步时间相关字段
--   2. 新增 sync_record 表记录同步历史
-- 变更原因：
--   支持增量同步策略：
--   - 首次同步：拉取30天内的数据
--   - 后续同步：只拉取24小时内变更的数据
-- ============================================================

-- -----------------------------------------------------------
-- 1. interface_config 表新增字段
-- -----------------------------------------------------------
ALTER TABLE `interface_config`
  ADD COLUMN `sync_time_field` VARCHAR(50) DEFAULT NULL COMMENT 'HIS时间字段名（如updateTime）' AFTER `on_failure`,
  ADD COLUMN `sync_time_param_start` VARCHAR(50) DEFAULT 'startTime' COMMENT '同步起始时间参数名' AFTER `sync_time_field`,
  ADD COLUMN `sync_time_param_end` VARCHAR(50) DEFAULT 'endTime' COMMENT '同步结束时间参数名' AFTER `sync_time_param_start`,
  ADD COLUMN `sync_time_format` VARCHAR(20) DEFAULT 'yyyy-MM-dd HH:mm:ss' COMMENT 'HIS时间格式' AFTER `sync_time_param_end`,
  ADD COLUMN `first_sync_days` INT DEFAULT 30 COMMENT '首次同步天数（默认30天）' AFTER `sync_time_format`,
  ADD COLUMN `incremental_sync_hours` INT DEFAULT 24 COMMENT '增量同步小时数（默认24小时）' AFTER `first_sync_days`,
  ADD COLUMN `last_sync_time` DATETIME DEFAULT NULL COMMENT '上次成功同步时间' AFTER `incremental_sync_hours`,
  ADD COLUMN `last_sync_status` VARCHAR(20) DEFAULT NULL COMMENT '上次同步状态（SUCCESS/FAILED/PARTIAL）' AFTER `last_sync_time`,
  ADD COLUMN `last_sync_count` INT DEFAULT NULL COMMENT '上次同步记录数' AFTER `last_sync_status`,
  ADD COLUMN `is_first_sync` TINYINT(1) DEFAULT 1 COMMENT '是否首次同步（1首次/0非首次）' AFTER `last_sync_count`;

-- -----------------------------------------------------------
-- 2. 新增 sync_record 表
-- 说明：记录每次同步的详细历史，便于追溯和问题排查
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sync_record`;
CREATE TABLE `sync_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_id` BIGINT NOT NULL COMMENT '接口配置ID',
  `config_code` VARCHAR(50) NOT NULL COMMENT '配置编码（冗余，便于查询）',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称（冗余，便于查询）',
  `sync_type` VARCHAR(20) NOT NULL COMMENT '同步类型（FIRST首次/INCREMENTAL增量/MANUAL手动）',
  `sync_start_time` DATETIME NOT NULL COMMENT '同步查询起始时间（传给HIS的startTime）',
  `sync_end_time` DATETIME NOT NULL COMMENT '同步查询结束时间（传给HIS的endTime）',
  `actual_start_time` DATETIME NOT NULL COMMENT '实际执行开始时间',
  `actual_end_time` DATETIME NOT NULL COMMENT '实际执行结束时间',
  `sync_status` VARCHAR(20) NOT NULL COMMENT '同步状态（SUCCESS/FAILED/PARTIAL）',
  `record_count` INT DEFAULT NULL COMMENT '同步记录数',
  `success_count` INT DEFAULT NULL COMMENT '成功写入数',
  `fail_count` INT DEFAULT NULL COMMENT '失败数',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `request_data` TEXT DEFAULT NULL COMMENT '请求数据（JSON）',
  `response_sample` TEXT DEFAULT NULL COMMENT '响应数据样例（前100条）',
  `duration_ms` INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID（手动同步时）',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_config_code` (`config_code`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `idx_actual_start_time` (`actual_start_time`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步记录表';

-- -----------------------------------------------------------
-- 3. 更新已有接口配置示例
-- -----------------------------------------------------------
-- 假设已有 HIS_DEPT_SYNC 配置，更新其增量同步参数
UPDATE `interface_config` 
SET 
  `sync_time_field` = 'updateTime',
  `sync_time_param_start` = 'startTime',
  `sync_time_param_end` = 'endTime',
  `sync_time_format` = 'yyyy-MM-dd HH:mm:ss',
  `first_sync_days` = 30,
  `incremental_sync_hours` = 24,
  `is_first_sync` = 1
WHERE `config_code` = 'HIS_DEPT_SYNC';

-- ============================================================
-- 变更结束
-- ============================================================