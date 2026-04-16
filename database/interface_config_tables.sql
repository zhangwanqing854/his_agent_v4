-- ============================================================
-- 接口配置相关表结构设计（支持多表映射）
-- ============================================================

-- -----------------------------------------------------------
-- 1. 接口配置主表 (interface_config)
-- 说明：存储三方接口的基本配置信息
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `interface_config`;
CREATE TABLE `interface_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_code` VARCHAR(50) NOT NULL COMMENT '配置编码，唯一标识',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `system` VARCHAR(50) NOT NULL COMMENT '三方系统（HIS/移动护理/CDR/其他）',
  `mode` VARCHAR(20) NOT NULL COMMENT '接口模式（PULL拉取/PUSH推送）',
  `protocol` VARCHAR(20) NOT NULL COMMENT '通讯协议（HTTP/HTTPS/WEBSOCKET）',
  `api_protocol` VARCHAR(20) NOT NULL COMMENT '接口协议（REST/SOAP/HL7/FHIR）',
  `method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法（GET/POST/PUT/DELETE）',
  `url` VARCHAR(500) NOT NULL COMMENT '接口地址',
  `data_type` VARCHAR(50) DEFAULT NULL COMMENT '数据类型（科室/医护/患者/诊断/医嘱等）',
  `sync_mode` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '同步模式（MANUAL手动/SCHEDULED定时/ON_DEMAND按需）',
  `sync_schedule` VARCHAR(100) DEFAULT NULL COMMENT '定时规则（Cron表达式）',
  `request_template` TEXT DEFAULT NULL COMMENT '请求模板（JSON格式）',
  `auth_type` VARCHAR(20) DEFAULT 'NONE' COMMENT '认证方式（NONE/BASIC/BEARER/API_KEY）',
  `auth_config` JSON DEFAULT NULL COMMENT '认证配置（JSON格式）',
  `retry_interval` INT DEFAULT 5 COMMENT '重试间隔（秒）',
  `max_retries` INT DEFAULT 3 COMMENT '最大重试次数',
  `on_failure` VARCHAR(20) DEFAULT 'ALERT' COMMENT '失败处理（STOP停止/SKIP跳过/ALERT告警）',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_code` (`config_code`),
  KEY `idx_system` (`system`),
  KEY `idx_data_type` (`data_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口配置主表';

-- -----------------------------------------------------------
-- 2. 映射表配置 (interface_mapping_table)
-- 说明：配置接口数据到目标表的映射，支持主子表结构
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `interface_mapping_table`;
CREATE TABLE `interface_mapping_table` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_id` BIGINT NOT NULL COMMENT '接口配置ID',
  `mapping_code` VARCHAR(50) NOT NULL COMMENT '映射编码',
  `mapping_name` VARCHAR(100) NOT NULL COMMENT '映射名称',
  `target_table` VARCHAR(50) NOT NULL COMMENT '目标表名',
  `data_path` VARCHAR(200) DEFAULT NULL COMMENT '数据路径（JSONPath，如 data.items 表示数组）',
  `is_array` TINYINT(1) DEFAULT 0 COMMENT '是否数组数据',
  `parent_mapping_id` BIGINT DEFAULT NULL COMMENT '父映射ID（用于子表关联主表）',
  `relation_field` VARCHAR(50) DEFAULT NULL COMMENT '关联字段（子表通过此字段关联主表）',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_mapping` (`config_id`, `mapping_code`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_target_table` (`target_table`),
  KEY `idx_parent_mapping_id` (`parent_mapping_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='映射表配置';

-- -----------------------------------------------------------
-- 3. 字段映射配置 (interface_field_mapping)
-- 说明：配置源字段到目标表字段的映射
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `interface_field_mapping`;
CREATE TABLE `interface_field_mapping` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mapping_table_id` BIGINT NOT NULL COMMENT '映射表配置ID',
  `source_field` VARCHAR(100) NOT NULL COMMENT '源字段（JSONPath或字段名）',
  `target_field` VARCHAR(50) NOT NULL COMMENT '目标字段',
  `transform_type` VARCHAR(20) DEFAULT 'DIRECT' COMMENT '转换类型（DIRECT直接/DATE日期/NUMBER数字/ENUM枚举/EXPRESSION表达式）',
  `transform_config` VARCHAR(500) DEFAULT NULL COMMENT '转换配置（JSON格式）',
  `default_value` VARCHAR(200) DEFAULT NULL COMMENT '默认值',
  `is_required` TINYINT(1) DEFAULT 1 COMMENT '是否必填',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_mapping_table_id` (`mapping_table_id`),
  KEY `idx_target_field` (`target_field`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段映射配置';

-- -----------------------------------------------------------
-- 示例数据：医嘱同步接口配置（主子表映射）
-- -----------------------------------------------------------

-- 1. 创建接口配置
INSERT INTO `interface_config` (`config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, `data_type`, `sync_mode`, `request_template`) VALUES
('HIS_ORDER_SYNC', 'HIS医嘱数据同步', 'HIS', 'PULL', 'HTTPS', 'REST', 'POST', 'https://his.hospital.com/api/orders', 'ORDER', 'SCHEDULED', '{"deptId": "{{deptId}}", "startTime": "{{startTime}}", "endTime": "{{endTime}}"}');

SET @config_id = LAST_INSERT_ID();

-- 2. 创建主表映射（order_main）
INSERT INTO `interface_mapping_table` (`config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`) VALUES
(@config_id, 'ORDER_MAIN', '医嘱主表映射', 'order_main', 'data.orders', 1, NULL, 1);

SET @main_mapping_id = LAST_INSERT_ID();

-- 3. 创建子表映射（order_item）
INSERT INTO `interface_mapping_table` (`config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `relation_field`, `sort_order`) VALUES
(@config_id, 'ORDER_ITEM', '医嘱子表映射', 'order_item', 'items', 1, @main_mapping_id, 'order_id', 2);

SET @item_mapping_id = LAST_INSERT_ID();

-- 4. 主表字段映射
INSERT INTO `interface_field_mapping` (`mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`) VALUES
(@main_mapping_id, 'orderId', 'order_no', 'DIRECT', 1, 1),
(@main_mapping_id, 'orderType', 'order_type', 'ENUM', 1, 2),
(@main_mapping_id, 'orderCategory', 'order_category', 'DIRECT', 1, 3),
(@main_mapping_id, 'doctorId', 'doctor_id', 'DIRECT', 1, 4),
(@main_mapping_id, 'doctorName', 'doctor_name', 'DIRECT', 1, 5),
(@main_mapping_id, 'orderTime', 'start_time', 'DATE', 1, 6),
(@main_mapping_id, 'status', 'status', 'DIRECT', 1, 7),
(@main_mapping_id, 'visitId', 'visit_id', 'DIRECT', 1, 8),
(@main_mapping_id, 'patientId', 'patient_id', 'DIRECT', 1, 9);

-- 5. 子表字段映射
INSERT INTO `interface_field_mapping` (`mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`) VALUES
(@item_mapping_id, 'itemCode', 'item_code', 'DIRECT', 1, 1),
(@item_mapping_id, 'itemName', 'item_name', 'DIRECT', 1, 2),
(@item_mapping_id, 'specification', 'specification', 'DIRECT', 0, 3),
(@item_mapping_id, 'dosage', 'dosage', 'DIRECT', 0, 4),
(@item_mapping_id, 'dosageUnit', 'dosage_unit', 'DIRECT', 0, 5),
(@item_mapping_id, 'frequency', 'frequency', 'DIRECT', 0, 6),
(@item_mapping_id, 'route', 'route', 'DIRECT', 0, 7),
(@item_mapping_id, 'orderTime', 'order_time', 'DATE', 1, 8);

-- ============================================================
-- 数据结构说明
-- ============================================================
-- 
-- 1. 接口返回数据结构示例：
-- {
--   "code": 0,
--   "data": {
--     "orders": [                    <-- data_path: data.orders
--       {
--         "orderId": "O001",         --> order_main.order_no
--         "orderType": "临时医嘱",    --> order_main.order_type
--         "orderTime": "2026-04-01T08:00:00",
--         "items": [                 <-- data_path: items (子表)
--           {
--             "itemCode": "D001",    --> order_item.item_code
--             "itemName": "药品A",    --> order_item.item_name
--             "dosage": "10mg"       --> order_item.dosage
--           }
--         ]
--       }
--     ]
--   }
-- }
--
-- 2. 映射关系：
-- interface_config (接口配置)
--   └── interface_mapping_table (映射表配置)
--         ├── ORDER_MAIN (主表映射) → order_main
--         │     └── interface_field_mapping (字段映射)
--         └── ORDER_ITEM (子表映射) → order_item
--               ├── parent_mapping_id = ORDER_MAIN.id
--               ├── relation_field = order_id (子表通过此字段关联主表)
--               └── interface_field_mapping (字段映射)
--
-- 3. 数据同步流程：
-- a. 调用接口获取数据
-- b. 根据 data_path 提取数据
-- c. 先插入主表数据，获取主键ID
-- d. 遍历子表数据，设置关联字段值，插入子表数据
-- ============================================================