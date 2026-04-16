-- ============================================================
-- 医护智能交接班质量管理系统 - 数据库建表脚本 V2.3
-- 北京大学国际医院
-- 创建日期：2026-04-08
-- 表数量：21张
-- 版本历史：
--   V1   - 初始设计，14张表
--   V2   - HIS子系统定位，患者与就诊分离，17张表
--   V2.1 - 新增接口配置表组，20张表
--   V2.2 - 增量同步支持，21张表
--   V2.3 - 补充编码字段
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第一部分：HIS同步表组（7张表）
-- ============================================================

DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `patient_no` VARCHAR(50) NOT NULL COMMENT '患者编号（HIS编码）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` VARCHAR(10) NOT NULL COMMENT '性别（男/女）',
  `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
  `age` INT DEFAULT NULL COMMENT '年龄（冗余字段）',
  `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话',
  `sync_time` DATETIME NOT NULL COMMENT '最后同步时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_no` (`patient_no`),
  KEY `idx_id_card` (`id_card`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者基本信息表';

DROP TABLE IF EXISTS `visit`;
CREATE TABLE `visit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `visit_no` VARCHAR(50) NOT NULL COMMENT '就诊号（HIS编码）',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `dept_id` BIGINT NOT NULL COMMENT '科室ID',
  `dept_name` VARCHAR(50) NOT NULL COMMENT '科室名称（冗余）',
  `bed_no` VARCHAR(20) DEFAULT NULL COMMENT '床位号',
  `admission_datetime` DATETIME NOT NULL COMMENT '入院时间',
  `discharge_datetime` DATETIME DEFAULT NULL COMMENT '出院时间',
  `doctor_id` BIGINT DEFAULT NULL COMMENT '主治医生ID',
  `doctor_name` VARCHAR(50) DEFAULT NULL COMMENT '主治医生姓名',
  `nurse_level` VARCHAR(20) NOT NULL COMMENT '护理级别（特级/一级/二级/三级）',
  `patient_status` VARCHAR(20) NOT NULL COMMENT '患者状态（在院/出院/转科/死亡）',
  `is_critical` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否危重患者',
  `sync_time` DATETIME NOT NULL COMMENT '最后同步时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_visit_no` (`visit_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_nurse_level` (`nurse_level`),
  KEY `idx_patient_status` (`patient_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就诊/住院信息表';

DROP TABLE IF EXISTS `transfer_record`;
CREATE TABLE `transfer_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `visit_id` BIGINT NOT NULL COMMENT '就诊ID',
  `visit_no` VARCHAR(50) NOT NULL COMMENT '就诊编码（HIS的code_ent）',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `patient_no` VARCHAR(50) DEFAULT NULL COMMENT '患者编码（HIS的code_pat）',
  `from_dept_id` BIGINT DEFAULT NULL COMMENT '转出科室ID',
  `from_dept_name` VARCHAR(50) DEFAULT NULL COMMENT '转出科室名称',
  `to_dept_id` BIGINT NOT NULL COMMENT '转入科室ID',
  `to_dept_name` VARCHAR(50) NOT NULL COMMENT '转入科室名称',
  `transfer_time` DATETIME NOT NULL COMMENT '转科时间',
  `transfer_type` VARCHAR(20) NOT NULL COMMENT '转科类型（入科/转出/转入）',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '转科原因',
  `sync_time` DATETIME NOT NULL COMMENT '最后同步时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_visit_id` (`visit_id`),
  KEY `idx_visit_no` (`visit_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_to_dept_id` (`to_dept_id`),
  KEY `idx_transfer_time` (`transfer_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转科记录表';

DROP TABLE IF EXISTS `diagnosis_main`;
CREATE TABLE `diagnosis_main` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `visit_id` BIGINT NOT NULL COMMENT '就诊ID',
  `visit_no` VARCHAR(50) NOT NULL COMMENT '就诊编码（HIS的code_ent）',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `patient_no` VARCHAR(50) NOT NULL COMMENT '患者编码（HIS的code_pat）',
  `diagnosis_type` VARCHAR(50) NOT NULL COMMENT '诊断类型（入院诊断/出院诊断/术前诊断/术后诊断等）',
  `diagnosis_time` DATETIME NOT NULL COMMENT '诊断时间',
  `doctor_id` BIGINT DEFAULT NULL COMMENT '诊断医生ID',
  `status` VARCHAR(20) NOT NULL COMMENT '状态（有效/作废）',
  `sync_time` DATETIME NOT NULL COMMENT '最后同步时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_visit_id` (`visit_id`),
  KEY `idx_visit_no` (`visit_no`),
  KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊断主表';

DROP TABLE IF EXISTS `diagnosis_item`;
CREATE TABLE `diagnosis_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `main_id` BIGINT NOT NULL COMMENT '诊断主表ID',
  `diagnosis_code` VARCHAR(50) NOT NULL COMMENT '诊断编码（ICD-10）',
  `diagnosis_name` VARCHAR(200) NOT NULL COMMENT '诊断名称',
  `is_main` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主诊断',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_main_id` (`main_id`),
  KEY `idx_is_main` (`is_main`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='诊断子表';

DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `visit_id` BIGINT NOT NULL COMMENT '就诊ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '医嘱号（HIS编码）',
  `order_type` VARCHAR(20) NOT NULL COMMENT '医嘱类型（长期医嘱/临时医嘱）',
  `order_category` VARCHAR(50) NOT NULL COMMENT '医嘱分类（药品/检查/检验/护理/治疗等）',
  `doctor_id` BIGINT NOT NULL COMMENT '开嘱医生ID',
  `doctor_name` VARCHAR(50) NOT NULL COMMENT '开嘱医生姓名',
  `start_time` DATETIME NOT NULL COMMENT '医嘱开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '医嘱结束时间',
  `status` VARCHAR(20) NOT NULL COMMENT '状态（有效/停止/作废）',
  `sync_time` DATETIME NOT NULL COMMENT '最后同步时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_visit_id` (`visit_id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_order_type` (`order_type`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医嘱主表';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `main_id` BIGINT NOT NULL COMMENT '医嘱主表ID',
  `item_code` VARCHAR(50) NOT NULL COMMENT '项目编码',
  `item_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
  `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
  `dosage` VARCHAR(50) DEFAULT NULL COMMENT '剂量',
  `dosage_unit` VARCHAR(20) DEFAULT NULL COMMENT '剂量单位',
  `frequency` VARCHAR(50) DEFAULT NULL COMMENT '频次（QD/BID/TID等）',
  `route` VARCHAR(50) DEFAULT NULL COMMENT '给药途径',
  `order_time` DATETIME NOT NULL COMMENT '医嘱时间',
  `execute_time` DATETIME DEFAULT NULL COMMENT '执行时间',
  `is_temporary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否临时医嘱',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_main_id` (`main_id`),
  KEY `idx_order_time` (`order_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医嘱子表';

-- ============================================================
-- 第二部分：交班业务表组（2张表）
-- ============================================================

DROP TABLE IF EXISTS `shift_handover`;
CREATE TABLE `shift_handover` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dept_id` BIGINT NOT NULL COMMENT '科室ID',
  `dept_name` VARCHAR(50) NOT NULL COMMENT '科室名称',
  `handover_date` DATE NOT NULL COMMENT '交班日期',
  `shift` VARCHAR(20) NOT NULL COMMENT '班次（白班/夜班）',
  `from_doctor_id` BIGINT NOT NULL COMMENT '交班医生ID',
  `from_doctor_name` VARCHAR(50) NOT NULL COMMENT '交班医生姓名',
  `to_doctor_id` BIGINT DEFAULT NULL COMMENT '接班医生ID',
  `to_doctor_name` VARCHAR(50) DEFAULT NULL COMMENT '接班医生姓名',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态（DRAFT/PENDING/TRANSFERRING/COMPLETED）',
  `summary_json` JSON DEFAULT NULL COMMENT '科室统计信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_handover_date` (`handover_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交班主表';

DROP TABLE IF EXISTS `handover_patient`;
CREATE TABLE `handover_patient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `handover_id` BIGINT NOT NULL COMMENT '交班ID',
  `visit_id` BIGINT NOT NULL COMMENT '就诊ID',
  `patient_id` BIGINT NOT NULL COMMENT '患者ID',
  `filter_reason` VARCHAR(50) NOT NULL COMMENT '筛选原因（NEW_ADMISSION/LEVEL1_NURSING/BOTH）',
  `bed_no` VARCHAR(20) NOT NULL COMMENT '床位号',
  `patient_name` VARCHAR(50) NOT NULL COMMENT '患者姓名',
  `gender` VARCHAR(10) NOT NULL COMMENT '性别',
  `age` INT NOT NULL COMMENT '年龄',
  `diagnosis` VARCHAR(500) DEFAULT NULL COMMENT '主诊断（从诊断子表获取is_main=1）',
  `vitals` TEXT DEFAULT NULL COMMENT '生命体征（可编辑）',
  `current_condition` TEXT DEFAULT NULL COMMENT '目前情况（含前12h临时医嘱，可编辑）',
  `observation_items` TEXT DEFAULT NULL COMMENT '需观察项（可编辑）',
  `mews_score` INT DEFAULT NULL COMMENT 'MEWS评分',
  `braden_score` INT DEFAULT NULL COMMENT 'Braden评分',
  `fall_risk` INT DEFAULT NULL COMMENT '跌倒风险评分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_handover_id` (`handover_id`),
  KEY `idx_visit_id` (`visit_id`),
  KEY `idx_filter_reason` (`filter_reason`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交班患者明细表';

-- ============================================================
-- 第三部分：用户权限表组（6张表）
-- ============================================================

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `is_super_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否超级管理员',
  `his_staff_id` BIGINT DEFAULT NULL COMMENT '关联HIS医生ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_his_staff_id` (`his_staff_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认角色',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(50) NOT NULL COMMENT '权限编码',
  `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

DROP TABLE IF EXISTS `duty`;
CREATE TABLE `duty` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(50) NOT NULL COMMENT '职责编码',
  `name` VARCHAR(50) NOT NULL COMMENT '职责名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '职责描述',
  `permission_id` BIGINT NOT NULL COMMENT '所属权限ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职责表';

DROP TABLE IF EXISTS `role_duty`;
CREATE TABLE `role_duty` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `duty_id` BIGINT NOT NULL COMMENT '职责ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_duty` (`role_id`, `duty_id`),
  KEY `idx_duty_id` (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-职责关联表';

DROP TABLE IF EXISTS `his_staff`;
CREATE TABLE `his_staff` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `staff_code` VARCHAR(50) NOT NULL COMMENT '工号（HIS编码）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `department_id` BIGINT NOT NULL COMMENT '科室ID',
  `title` VARCHAR(50) DEFAULT NULL COMMENT '职称',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
  `sync_time` DATETIME NOT NULL COMMENT '最后同步时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_code` (`staff_code`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HIS医生信息表';

-- ============================================================
-- 第四部分：科室组织表组（2张表）
-- ============================================================

DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '科室名称',
  `code` VARCHAR(50) NOT NULL COMMENT '科室编码（HIS编码）',
  `bed_count` INT NOT NULL DEFAULT 0 COMMENT '床位数量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室表';

DROP TABLE IF EXISTS `doctor_department`;
CREATE TABLE `doctor_department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `department_id` BIGINT NOT NULL COMMENT '科室ID',
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主科室',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生-科室关联表';

-- ============================================================
-- 第五部分：预置数据
-- ============================================================

INSERT INTO `role` (`id`, `name`, `code`, `is_default`, `description`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 0, '系统超级管理员，拥有所有权限'),
(2, '普通医生', 'DOCTOR', 1, '普通医生角色，HIS同步医生的默认角色');

INSERT INTO `permission` (`id`, `code`, `name`, `description`) VALUES
(1, 'HANDOVER', '交班管理', '交班记录管理相关功能'),
(2, 'PATIENT', '患者管理', '患者信息管理相关功能'),
(3, 'TODO', '待办事项', '待办事项管理相关功能'),
(4, 'STATISTICS', '统计分析', '统计数据查看相关功能'),
(5, 'USER_MANAGE', '用户管理', '用户账号管理相关功能'),
(6, 'ROLE_MANAGE', '角色权限', '角色权限管理相关功能'),
(7, 'SYSTEM_SETTINGS', '系统设置', '系统配置管理相关功能');

INSERT INTO `duty` (`id`, `code`, `name`, `description`, `permission_id`) VALUES
(1, 'HANDOVER_VIEW', '查看交班记录', '查看交班记录列表和详情', 1),
(2, 'HANDOVER_CREATE', '发起交班', '创建新的交班记录', 1),
(3, 'HANDOVER_EDIT', '编辑交班记录', '编辑交班记录内容', 1),
(4, 'HANDOVER_DELETE', '删除交班记录', '删除交班记录', 1),
(5, 'HANDOVER_REVIEW', '审核交班', '审核交班记录', 1),
(10, 'PATIENT_VIEW', '查看患者信息', '查看患者列表和详情', 2),
(11, 'PATIENT_EXPORT', '导出患者数据', '导出患者信息', 2),
(20, 'TODO_VIEW', '查看待办事项', '查看待办事项列表', 3),
(21, 'TODO_HANDLE', '处理待办事项', '处理待办事项', 3),
(30, 'STATISTICS_VIEW', '查看统计报表', '查看统计报表', 4),
(31, 'STATISTICS_EXPORT', '导出统计报表', '导出统计数据', 4),
(40, 'USER_VIEW', '查看用户列表', '查看用户列表和详情', 5),
(41, 'USER_CREATE', '新增用户', '创建新用户账号', 5),
(42, 'USER_EDIT', '编辑用户', '编辑用户信息', 5),
(43, 'USER_DELETE', '删除用户', '删除用户账号', 5),
(44, 'USER_ENABLE', '启用/禁用用户', '启用或禁用用户账号', 5),
(45, 'USER_RESET_PWD', '重置密码', '重置用户密码', 5),
(50, 'ROLE_VIEW', '查看角色列表', '查看角色列表和详情', 6),
(51, 'ROLE_CREATE', '新增角色', '创建新角色', 6),
(52, 'ROLE_EDIT', '编辑角色', '编辑角色信息', 6),
(53, 'ROLE_DELETE', '删除角色', '删除角色', 6),
(54, 'ROLE_ASSIGN_DUTY', '分配职责', '为角色分配职责', 6),
(60, 'SETTINGS_VIEW', '查看系统配置', '查看系统配置', 7),
(61, 'SETTINGS_EDIT', '修改系统配置', '修改系统配置', 7),
(62, 'INTERFACE_CONFIG', '接口配置管理', '管理接口配置', 7),
(63, 'INTERFACE_SYNC', '数据同步', '执行数据同步', 7);

INSERT INTO `role_duty` (`role_id`, `duty_id`) SELECT 1, id FROM `duty`;

INSERT INTO `role_duty` (`role_id`, `duty_id`) VALUES
(2, 1), (2, 2), (2, 3), (2, 4),
(2, 10), (2, 11),
(2, 20), (2, 21),
(2, 30), (2, 31);

INSERT INTO `user` (`id`, `username`, `password`, `is_super_admin`, `his_staff_id`, `role_id`, `enabled`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, NULL, 1, 1);

INSERT INTO `department` (`id`, `name`, `code`, `bed_count`) VALUES
(1, '心内科', 'XNK', 50),
(2, '神经内科', 'SJNK', 40),
(3, '呼吸科', 'HXK', 35),
(4, '内分泌科', 'NFMK', 30);

-- ============================================================
-- 第六部分：接口配置表组（3张表）
-- ============================================================

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
  `sync_time_field` VARCHAR(50) DEFAULT NULL COMMENT 'HIS时间字段名',
  `sync_time_param_start` VARCHAR(50) DEFAULT 'startTime' COMMENT '同步起始时间参数名',
  `sync_time_param_end` VARCHAR(50) DEFAULT 'endTime' COMMENT '同步结束时间参数名',
  `sync_time_format` VARCHAR(20) DEFAULT 'yyyy-MM-dd HH:mm:ss' COMMENT 'HIS时间格式',
  `first_sync_days` INT DEFAULT 30 COMMENT '首次同步天数',
  `incremental_sync_hours` INT DEFAULT 24 COMMENT '增量同步小时数',
  `last_sync_time` DATETIME DEFAULT NULL COMMENT '上次成功同步时间',
  `last_sync_status` VARCHAR(20) DEFAULT NULL COMMENT '上次同步状态',
  `last_sync_count` INT DEFAULT NULL COMMENT '上次同步记录数',
  `is_first_sync` TINYINT(1) DEFAULT 1 COMMENT '是否首次同步',
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

DROP TABLE IF EXISTS `interface_mapping_table`;
CREATE TABLE `interface_mapping_table` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_id` BIGINT NOT NULL COMMENT '接口配置ID',
  `mapping_code` VARCHAR(50) NOT NULL COMMENT '映射编码',
  `mapping_name` VARCHAR(100) NOT NULL COMMENT '映射名称',
  `target_table` VARCHAR(50) NOT NULL COMMENT '目标表名',
  `data_path` VARCHAR(200) DEFAULT NULL COMMENT '数据路径（JSONPath）',
  `is_array` TINYINT(1) DEFAULT 0 COMMENT '是否数组数据',
  `parent_mapping_id` BIGINT DEFAULT NULL COMMENT '父映射ID',
  `relation_field` VARCHAR(50) DEFAULT NULL COMMENT '关联字段',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_mapping` (`config_id`, `mapping_code`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_target_table` (`target_table`),
  KEY `idx_parent_mapping_id` (`parent_mapping_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='映射表配置';

DROP TABLE IF EXISTS `interface_field_mapping`;
CREATE TABLE `interface_field_mapping` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mapping_table_id` BIGINT NOT NULL COMMENT '映射表配置ID',
  `source_field` VARCHAR(100) NOT NULL COMMENT '源字段',
  `target_field` VARCHAR(50) NOT NULL COMMENT '目标字段',
  `transform_type` VARCHAR(20) DEFAULT 'DIRECT' COMMENT '转换类型',
  `transform_config` VARCHAR(500) DEFAULT NULL COMMENT '转换配置',
  `default_value` VARCHAR(200) DEFAULT NULL COMMENT '默认值',
  `is_required` TINYINT(1) DEFAULT 1 COMMENT '是否必填',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_mapping_table_id` (`mapping_table_id`),
  KEY `idx_target_field` (`target_field`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段映射配置';

-- ============================================================
-- 第七部分：同步记录表（1张表）
-- ============================================================

DROP TABLE IF EXISTS `sync_record`;
CREATE TABLE `sync_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_id` BIGINT NOT NULL COMMENT '接口配置ID',
  `config_code` VARCHAR(50) NOT NULL COMMENT '配置编码',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `sync_type` VARCHAR(20) NOT NULL COMMENT '同步类型（FIRST首次/INCREMENTAL增量/MANUAL手动）',
  `sync_start_time` DATETIME NOT NULL COMMENT '同步查询起始时间',
  `sync_end_time` DATETIME NOT NULL COMMENT '同步查询结束时间',
  `actual_start_time` DATETIME NOT NULL COMMENT '实际执行开始时间',
  `actual_end_time` DATETIME NOT NULL COMMENT '实际执行结束时间',
  `sync_status` VARCHAR(20) NOT NULL COMMENT '同步状态（SUCCESS/FAILED/PARTIAL）',
  `record_count` INT DEFAULT NULL COMMENT '同步记录数',
  `success_count` INT DEFAULT NULL COMMENT '成功写入数',
  `fail_count` INT DEFAULT NULL COMMENT '失败数',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `request_data` TEXT DEFAULT NULL COMMENT '请求数据（JSON）',
  `response_sample` TEXT DEFAULT NULL COMMENT '响应数据样例',
  `duration_ms` INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_config_code` (`config_code`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `idx_actual_start_time` (`actual_start_time`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步记录表';

SET FOREIGN_KEY_CHECKS = 1;