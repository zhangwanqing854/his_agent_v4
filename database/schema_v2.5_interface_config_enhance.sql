-- ============================================================
-- 接口配置表增强字段更新 (v2.5)
-- 说明：为 interface_config 表添加 SOAP 支持和增量同步相关字段
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. 添加 SOAP 相关字段
-- -----------------------------------------------------------
ALTER TABLE `interface_config`
ADD COLUMN `soap_action` VARCHAR(100) DEFAULT NULL COMMENT 'SOAP Action' AFTER `enabled`,
ADD COLUMN `soap_namespace` VARCHAR(200) DEFAULT NULL COMMENT 'SOAP命名空间' AFTER `soap_action`,
ADD COLUMN `soap_params` JSON DEFAULT NULL COMMENT 'SOAP参数配置' AFTER `soap_namespace`;

-- -----------------------------------------------------------
-- 2. 添加增量同步相关字段
-- -----------------------------------------------------------
ALTER TABLE `interface_config`
ADD COLUMN `sync_time_param_start` VARCHAR(50) DEFAULT NULL COMMENT '同步开始时间参数名' AFTER `soap_params`,
ADD COLUMN `sync_time_param_end` VARCHAR(50) DEFAULT NULL COMMENT '同步结束时间参数名' AFTER `sync_time_param_start`,
ADD COLUMN `sync_time_format` VARCHAR(50) DEFAULT 'yyyy-MM-dd HH:mm:ss' COMMENT '同步时间格式' AFTER `sync_time_param_end`,
ADD COLUMN `first_sync_days` INT DEFAULT 0 COMMENT '首次同步天数' AFTER `sync_time_format`,
ADD COLUMN `incremental_sync_hours` INT DEFAULT 24 COMMENT '增量同步小时数' AFTER `first_sync_days`,
ADD COLUMN `is_first_sync` TINYINT(1) DEFAULT 1 COMMENT '是否首次同步' AFTER `incremental_sync_hours`,
ADD COLUMN `last_sync_time` DATETIME DEFAULT NULL COMMENT '最后同步时间' AFTER `is_first_sync`,
ADD COLUMN `last_sync_status` VARCHAR(20) DEFAULT NULL COMMENT '最后同步状态（SUCCESS/FAILED/PARTIAL）' AFTER `last_sync_time`,
ADD COLUMN `last_sync_count` INT DEFAULT NULL COMMENT '最后同步数量' AFTER `last_sync_status`;

-- -----------------------------------------------------------
-- 3. 添加索引（可选，根据查询需求）
-- -----------------------------------------------------------
ALTER TABLE `interface_config`
ADD KEY `idx_sync_mode` (`sync_mode`),
ADD KEY `idx_last_sync_status` (`last_sync_status`);

-- ============================================================
-- 更新说明
-- ============================================================
-- 
-- 新增字段说明：
-- 1. soap_action: SOAP请求的动作名称，如 'getDept', 'getEmp' 等
-- 2. soap_namespace: SOAP命名空间，如 'http://i.sync.common.pkuih.iih/'
-- 3. soap_params: SOAP参数配置（JSON格式），如 [{"name":"arg0","value":""}, {"name":"arg1","value":""}]
-- 4. sync_time_param_start: 同步开始时间参数名（用于增量同步）
-- 5. sync_time_param_end: 同步结束时间参数名（用于增量同步）
-- 6. sync_time_format: 时间格式，默认 'yyyy-MM-dd HH:mm:ss'
-- 7. first_sync_days: 首次同步天数，0表示从当前时间开始
-- 8. incremental_sync_hours: 增量同步小时数，默认24小时
-- 9. is_first_sync: 是否首次同步，1=是，0=否
-- 10. last_sync_time: 最后同步时间
-- 11. last_sync_status: 最后同步状态（SUCCESS/FAILED/PARTIAL）
-- 12. last_sync_count: 最后同步数量
--
-- 使用示例：
-- 首次同步：设置 is_first_sync=1, first_sync_days=30（同步最近30天数据）
-- 增量同步：设置 is_first_sync=0, incremental_sync_hours=24（同步最近24小时数据）
-- ============================================================
