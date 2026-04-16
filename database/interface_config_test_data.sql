-- ============================================================
-- 接口配置测试数据 (v2.5)
-- 说明：基于 mock/interface-config.ts 生成的7个接口配置测试数据
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. HIS_DEPT_SYNC - 科室信息同步
-- -----------------------------------------------------------
INSERT INTO `interface_config` (
  `config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, 
  `data_type`, `sync_mode`, `sync_schedule`, `request_template`, `auth_type`, 
  `retry_interval`, `max_retries`, `on_failure`, `enabled`,
  `soap_action`, `soap_namespace`, `soap_params`,
  `sync_time_param_start`, `sync_time_param_end`, `sync_time_format`,
  `first_sync_days`, `incremental_sync_hours`, `is_first_sync`
) VALUES (
  'HIS_DEPT_SYNC', 'HIS科室信息同步', 'HIS', 'PULL', 'HTTP', 'SOAP', NULL, 
  'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
  'DEPARTMENT', 'MANUAL', '', NULL, 'NONE',
  5, 3, 'ALERT', 1,
  'getDept', 'http://i.sync.common.pkuih.iih/',
  '[{"name":"arg0","value":""},{"name":"arg1","value":""}]',
  'arg0', 'arg1', 'yyyy-MM-dd HH:mm:ss',
  0, 24, 1
);

SET @config_id_1 = LAST_INSERT_ID();

INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`
) VALUES (
  @config_id_1, 'DEPT_MAIN', '科室主表映射', 'department', 'return', 1, NULL, 0
);

SET @mapping_id_1 = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_1, 'id_dep', 'his_id', 'DIRECT', 1, 0),
  (@mapping_id_1, 'code', 'code', 'DIRECT', 1, 1),
  (@mapping_id_1, 'name', 'name', 'DIRECT', 1, 2);

-- -----------------------------------------------------------
-- 2. HIS_STAFF_SYNC - 人员信息同步
-- -----------------------------------------------------------
INSERT INTO `interface_config` (
  `config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, 
  `data_type`, `sync_mode`, `sync_schedule`, `request_template`, `auth_type`, 
  `retry_interval`, `max_retries`, `on_failure`, `enabled`,
  `soap_action`, `soap_namespace`, `soap_params`,
  `sync_time_param_start`, `sync_time_param_end`, `sync_time_format`,
  `first_sync_days`, `incremental_sync_hours`, `is_first_sync`
) VALUES (
  'HIS_STAFF_SYNC', 'HIS人员信息同步', 'HIS', 'PULL', 'HTTP', 'SOAP', NULL, 
  'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?wsdl&access_token=cfa0c38f-bc0e-4914-83b6-f76b485bc8ae',
  'STAFF', 'SCHEDULED', '0 0 7 * * ?', NULL, 'NONE',
  5, 3, 'ALERT', 1,
  'getEmp', 'http://i.sync.common.pkuih.iih/',
  '[{"name":"arg0","value":""},{"name":"arg1","value":""}]',
  'arg0', 'arg1', 'yyyy-MM-dd HH:mm:ss',
  0, 24, 1
);

SET @config_id_2 = LAST_INSERT_ID();

INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`
) VALUES (
  @config_id_2, 'STAFF_MAIN', '人员主表映射', 'his_staff', 'return', 1, NULL, 0
);

SET @mapping_id_2 = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_2, 'code_user', 'staff_code', 'DIRECT', 1, 0),
  (@mapping_id_2, 'name_user', 'name', 'DIRECT', 1, 1),
  (@mapping_id_2, 'sd_emptitle', 'title_code', 'DIRECT', 0, 2),
  (@mapping_id_2, 'name_emptitle', 'title', 'DIRECT', 0, 3);

-- -----------------------------------------------------------
-- 3. HIS_PATIENT_SYNC - 患者信息同步
-- -----------------------------------------------------------
INSERT INTO `interface_config` (
  `config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, 
  `data_type`, `sync_mode`, `sync_schedule`, `request_template`, `auth_type`, 
  `retry_interval`, `max_retries`, `on_failure`, `enabled`,
  `soap_action`, `soap_namespace`, `soap_params`,
  `sync_time_param_start`, `sync_time_param_end`, `sync_time_format`,
  `first_sync_days`, `incremental_sync_hours`, `is_first_sync`
) VALUES (
  'HIS_PATIENT_SYNC', 'HIS患者信息同步', 'HIS', 'PULL', 'HTTP', 'SOAP', NULL, 
  'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
  'PATIENT', 'SCHEDULED', '0 0 7,16 * * ?', NULL, 'NONE',
  5, 3, 'ALERT', 1,
  'getPiPat', 'http://i.sync.common.pkuih.iih/',
  '[{"name":"arg0","value":""},{"name":"arg1","value":""}]',
  'arg0', 'arg1', 'yyyy-MM-dd HH:mm:ss',
  30, 24, 1
);

SET @config_id_3 = LAST_INSERT_ID();

INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`
) VALUES (
  @config_id_3, 'PATIENT_MAIN', '患者主表映射', 'patient', 'return', 1, NULL, 0
);

SET @mapping_id_3 = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_3, 'code', 'patient_no', 'DIRECT', 1, 0),
  (@mapping_id_3, 'name', 'name', 'DIRECT', 1, 1),
  (@mapping_id_3, 'sd_sex', 'gender_code', 'DIRECT', 1, 2),
  (@mapping_id_3, 'name_sex', 'gender', 'DIRECT', 1, 3),
  (@mapping_id_3, 'dt_birth', 'birth_date', 'DATE', 0, 4),
  (@mapping_id_3, 'mob', 'phone', 'DIRECT', 0, 5);

-- -----------------------------------------------------------
-- 4. HIS_VISIT_SYNC - 就诊信息同步
-- -----------------------------------------------------------
INSERT INTO `interface_config` (
  `config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, 
  `data_type`, `sync_mode`, `sync_schedule`, `request_template`, `auth_type`, 
  `retry_interval`, `max_retries`, `on_failure`, `enabled`,
  `soap_action`, `soap_namespace`, `soap_params`,
  `sync_time_param_start`, `sync_time_param_end`, `sync_time_format`,
  `first_sync_days`, `incremental_sync_hours`, `is_first_sync`
) VALUES (
  'HIS_VISIT_SYNC', 'HIS就诊信息同步', 'HIS', 'PULL', 'HTTP', 'SOAP', NULL, 
  'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
  'VISIT', 'SCHEDULED', '0 0 7,16 * * ?', NULL, 'NONE',
  5, 3, 'ALERT', 1,
  'getEnt', 'http://i.sync.common.pkuih.iih/',
  '[{"name":"arg0","value":""},{"name":"arg1","value":""}]',
  'arg0', 'arg1', 'yyyy-MM-dd HH:mm:ss',
  30, 24, 1
);

SET @config_id_4 = LAST_INSERT_ID();

INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`
) VALUES (
  @config_id_4, 'VISIT_MAIN', '就诊主表映射', 'visit', 'return', 1, NULL, 0
);

SET @mapping_id_4 = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_4, 'code_ent', 'visit_no', 'DIRECT', 1, 0),
  (@mapping_id_4, 'code_pat', 'patient_no', 'DIRECT', 1, 1),
  (@mapping_id_4, 'name_pat', 'patient_name', 'DIRECT', 1, 2),
  (@mapping_id_4, 'id_pat', 'patient_his_id', 'DIRECT', 1, 3),
  (@mapping_id_4, 'code_dep_phy', 'dept_his_id', 'DIRECT', 1, 4),
  (@mapping_id_4, 'name_dep_phy', 'dept_name', 'DIRECT', 1, 5),
  (@mapping_id_4, 'sd_level_nur', 'nurse_level_code', 'DIRECT', 1, 6),
  (@mapping_id_4, 'name_level_nur', 'nurse_level', 'DIRECT', 1, 7),
  (@mapping_id_4, 'dt_acpt', 'admission_datetime', 'DATE', 1, 8),
  (@mapping_id_4, 'dt_end', 'discharge_datetime', 'DATE', 0, 9),
  (@mapping_id_4, 'code_entp', 'visit_type', 'DIRECT', 0, 10),
  (@mapping_id_4, 'times_ip', 'admission_count', 'DIRECT', 0, 11),
  (@mapping_id_4, 'id_dep_nur', 'nurse_area_id', 'DIRECT', 0, 12),
  (@mapping_id_4, 'code_dep_nur', 'nurse_area_code', 'DIRECT', 0, 13),
  (@mapping_id_4, 'name_dep_nur', 'nurse_area_name', 'DIRECT', 0, 14);

-- -----------------------------------------------------------
-- 5. HIS_TRANSFER_SYNC - 转科信息同步
-- -----------------------------------------------------------
INSERT INTO `interface_config` (
  `config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, 
  `data_type`, `sync_mode`, `sync_schedule`, `request_template`, `auth_type`, 
  `retry_interval`, `max_retries`, `on_failure`, `enabled`,
  `soap_action`, `soap_namespace`, `soap_params`,
  `sync_time_param_start`, `sync_time_param_end`, `sync_time_format`,
  `first_sync_days`, `incremental_sync_hours`, `is_first_sync`
) VALUES (
  'HIS_TRANSFER_SYNC', 'HIS转科信息同步', 'HIS', 'PULL', 'HTTP', 'SOAP', NULL, 
  'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
  'TRANSFER', 'SCHEDULED', '0 30 7,16 * * ?', NULL, 'NONE',
  5, 3, 'ALERT', 1,
  'getEntDept', 'http://i.sync.common.pkuih.iih/',
  '[{"name":"arg0","value":""},{"name":"arg1","value":""}]',
  'arg0', 'arg1', 'yyyy-MM-dd HH:mm:ss',
  30, 24, 1
);

SET @config_id_5 = LAST_INSERT_ID();

INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`
) VALUES (
  @config_id_5, 'TRANSFER_MAIN', '转科记录映射', 'transfer_record', 'return', 1, NULL, 0
);

SET @mapping_id_5 = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_5, 'code_ent', 'visit_no', 'DIRECT', 1, 0),
  (@mapping_id_5, 'from_dep_code', 'from_dept_code', 'DIRECT', 0, 1),
  (@mapping_id_5, 'from_dep_name', 'from_dept_name', 'DIRECT', 0, 2),
  (@mapping_id_5, 'to_dep_code', 'to_dept_code', 'DIRECT', 1, 3),
  (@mapping_id_5, 'to_dep_name', 'to_dept_name', 'DIRECT', 1, 4),
  (@mapping_id_5, 'sv', 'transfer_time', 'DATE', 1, 5);

-- -----------------------------------------------------------
-- 6. HIS_DIAGNOSIS_SYNC - 诊断信息同步
-- -----------------------------------------------------------
INSERT INTO `interface_config` (
  `config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, 
  `data_type`, `sync_mode`, `sync_schedule`, `request_template`, `auth_type`, 
  `retry_interval`, `max_retries`, `on_failure`, `enabled`,
  `soap_action`, `soap_namespace`, `soap_params`,
  `sync_time_param_start`, `sync_time_param_end`, `sync_time_format`,
  `first_sync_days`, `incremental_sync_hours`, `is_first_sync`
) VALUES (
  'HIS_DIAGNOSIS_SYNC', 'HIS诊断信息同步', 'HIS', 'PULL', 'HTTP', 'SOAP', NULL, 
  'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
  'DIAGNOSIS', 'ON_DEMAND', '', NULL, 'NONE',
  5, 3, 'ALERT', 1,
  'getDiadef', 'http://i.sync.common.pkuih.iih/',
  '[{"name":"arg0","value":""}]',
  NULL, NULL, 'yyyy-MM-dd HH:mm:ss',
  0, 24, 1
);

SET @config_id_6 = LAST_INSERT_ID();

INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`
) VALUES (
  @config_id_6, 'DIAGNOSIS_MAIN', '诊断主表映射', 'diagnosis_main', 'return', 1, NULL, 0
);

SET @mapping_id_6 = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_6, 'code_ent', 'visit_no', 'DIRECT', 1, 0),
  (@mapping_id_6, 'code_pat', 'patient_no', 'DIRECT', 1, 1),
  (@mapping_id_6, 'sd_ditp', 'diagnosis_type_code', 'DIRECT', 1, 2),
  (@mapping_id_6, 'name_ditp', 'diagnosis_type', 'DIRECT', 1, 3),
  (@mapping_id_6, 'dt_di', 'diagnosis_time', 'DATE', 1, 4),
  (@mapping_id_6, 'id_di', 'his_id', 'DIRECT', 1, 5);

-- 子表映射
INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `relation_field`, `sort_order`
) VALUES (
  @config_id_6, 'DIAGNOSIS_ITEM', '诊断子表映射', 'diagnosis_item', 'diitm', 1, @mapping_id_6, 'main_his_id', 0
);

SET @mapping_id_6_child = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_6_child, 'code_didef', 'diagnosis_code', 'DIRECT', 1, 0),
  (@mapping_id_6_child, 'name_didef', 'diagnosis_name', 'DIRECT', 1, 1),
  (@mapping_id_6_child, 'fg_majdi', 'is_main', 'DIRECT', 1, 2),
  (@mapping_id_6_child, 'sortno', 'sort_order', 'NUMBER', 0, 3);

-- -----------------------------------------------------------
-- 7. HIS_ORDER_SYNC - 医嘱信息同步
-- -----------------------------------------------------------
INSERT INTO `interface_config` (
  `config_code`, `config_name`, `system`, `mode`, `protocol`, `api_protocol`, `method`, `url`, 
  `data_type`, `sync_mode`, `sync_schedule`, `request_template`, `auth_type`, 
  `retry_interval`, `max_retries`, `on_failure`, `enabled`,
  `soap_action`, `soap_namespace`, `soap_params`,
  `sync_time_param_start`, `sync_time_param_end`, `sync_time_format`,
  `first_sync_days`, `incremental_sync_hours`, `is_first_sync`
) VALUES (
  'HIS_ORDER_SYNC', 'HIS医嘱信息同步', 'HIS', 'PULL', 'HTTP', 'SOAP', NULL, 
  'http://10.193.162.82:8089/iih.pkuih.common.sync.i.IHISSyncHoService?p=YWNjZXNzX3Rva2VuPWNmYTBjMzhmLWJjMGUtNDkxNC04M2I2LWY3NmI0ODViYzhhZQ==',
  'ORDER', 'ON_DEMAND', '', NULL, 'NONE',
  5, 3, 'ALERT', 1,
  'getOrders', 'http://i.sync.common.pkuih.iih/',
  '[{"name":"arg0","value":""},{"name":"arg1","value":""},{"name":"arg2","value":""}]',
  'arg1', 'arg2', 'yyyy-MM-dd HH:mm:ss',
  30, 24, 1
);

SET @config_id_7 = LAST_INSERT_ID();

INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `sort_order`
) VALUES (
  @config_id_7, 'ORDER_MAIN', '医嘱主表映射', 'order_main', 'return', 1, NULL, 0
);

SET @mapping_id_7 = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_7, 'id_or', 'his_id', 'DIRECT', 1, 0),
  (@mapping_id_7, 'code_ent', 'visit_no', 'DIRECT', 1, 1),
  (@mapping_id_7, 'code_pat', 'patient_no', 'DIRECT', 1, 2),
  (@mapping_id_7, 'code_or', 'order_no', 'DIRECT', 1, 3),
  (@mapping_id_7, 'name_or', 'order_name', 'DIRECT', 1, 4),
  (@mapping_id_7, 'fg_long', 'order_type', 'ENUM', 1, 5),
  (@mapping_id_7, 'sd_srvtp', 'service_type', 'DIRECT', 1, 6),
  (@mapping_id_7, 'dt_effe', 'start_time', 'DATE', 1, 7),
  (@mapping_id_7, 'dt_end', 'end_time', 'DATE', 0, 8);

-- 子表映射
INSERT INTO `interface_mapping_table` (
  `config_id`, `mapping_code`, `mapping_name`, `target_table`, `data_path`, `is_array`, `parent_mapping_id`, `relation_field`, `sort_order`
) VALUES (
  @config_id_7, 'ORDER_ITEM', '医嘱子表映射', 'order_item', 'ciitm', 1, @mapping_id_7, 'main_his_id', 0
);

SET @mapping_id_7_child = LAST_INSERT_ID();

INSERT INTO `interface_field_mapping` (
  `mapping_table_id`, `source_field`, `target_field`, `transform_type`, `is_required`, `sort_order`
) VALUES
  (@mapping_id_7_child, 'id_orsrv', 'his_id', 'DIRECT', 1, 0),
  (@mapping_id_7_child, 'name', 'item_name', 'DIRECT', 1, 1),
  (@mapping_id_7_child, 'quan_medu', 'dosage', 'DIRECT', 0, 2),
  (@mapping_id_7_child, 'name_medu', 'dosage_unit', 'DIRECT', 0, 3),
  (@mapping_id_7_child, 'name_route', 'route', 'DIRECT', 0, 4);

-- ============================================================
-- 测试数据说明
-- ============================================================
-- 
-- 1. 所有配置的 enabled 字段均为 1（启用）
-- 2. auth_type 均为 NONE（无需认证）
-- 3. retry_interval 均为 5 秒，max_retries 均为 3 次
-- 4. onFailure 均为 ALERT（失败告警）
-- 5. SOAP 协议的接口均使用相同的命名空间：http://i.sync.common.pkuih.iih/
-- 6. HIS_DEPT_SYNC 为手动同步模式，其他为定时或按需同步
-- 7. HIS_PATIENT_SYNC 和 HIS_VISIT_SYNC 的 first_sync_days 为 30 天
-- 8. HIS_DIAGNOSIS_SYNC 和 HIS_ORDER_SYNC 为子表映射结构
-- ============================================================
