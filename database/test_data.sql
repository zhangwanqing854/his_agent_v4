-- ============================================================
-- 测试数据脚本
-- ============================================================

SET NAMES utf8mb4;

-- 清空现有数据（注意顺序，先删除有外键依赖的表）
DELETE FROM handover_patient;
DELETE FROM shift_handover;
DELETE FROM doctor_department;
DELETE FROM `user`;
DELETE FROM `role`;
DELETE FROM department;
DELETE FROM `visit`;
DELETE FROM patient;

-- 插入科室数据
INSERT INTO department (id, code, name, bed_count, created_at, updated_at) VALUES
(1, '0000098', '心内科', 50, NOW(), NOW()),
(2, '0000094', '神经内科', 40, NOW(), NOW()),
(3, '0000109', '呼吸科', 30, NOW(), NOW()),
(4, '0000110', '内分泌科', 25, NOW(), NOW());

-- 插入角色数据
INSERT INTO `role` (id, code, name, description, is_default, created_at, updated_at) VALUES
(1, 'ADMIN', '系统管理员', '系统管理员角色', 0, NOW(), NOW()),
(2, 'DOCTOR', '医生', '医生角色', 1, NOW(), NOW()),
(3, 'NURSE', '护士', '护士角色', 0, NOW(), NOW());

-- 插入用户数据 (密码为明文 '123456' 和 'admin')
INSERT INTO `user` (id, username, password, role_id, his_staff_id, is_super_admin, enabled, created_at, updated_at) VALUES
(1, 'admin', 'admin', 1, NULL, 1, 1, NOW(), NOW()),
(2, 'doctor1', '123456', 2, NULL, 0, 1, NOW(), NOW()),
(3, 'doctor2', '123456', 2, NULL, 0, 1, NOW(), NOW());

-- 插入医生-科室关联
INSERT INTO doctor_department (id, doctor_id, department_id, is_primary, created_at) VALUES
(1, 2, 1, 1, NOW()),
(2, 2, 2, 0, NOW()),
(3, 3, 2, 1, NOW());

-- 插入患者数据
INSERT INTO patient (id, patient_no, name, gender, age, phone, sync_time, created_at, updated_at) VALUES
(1, 'P001', '张三', '男', 65, '13800001001', NOW(), NOW(), NOW()),
(2, 'P002', '李四', '男', 58, '13800001002', NOW(), NOW(), NOW()),
(3, 'P003', '王五', '男', 72, '13800001003', NOW(), NOW(), NOW()),
(4, 'P004', '赵六', '女', 55, '13800001004', NOW(), NOW(), NOW()),
(5, 'P005', '钱七', '男', 48, '13800001005', NOW(), NOW(), NOW()),
(6, 'P006', '孙八', '男', 68, '13800001006', NOW(), NOW(), NOW()),
(7, 'P007', '周九', '女', 70, '13800001007', NOW(), NOW(), NOW()),
(8, 'P008', '吴十', '男', 45, '13800001008', NOW(), NOW(), NOW());

-- 插入就诊数据 (包含 nurse_level_code)
-- 一级护理 code='01', 二级 code='02', 三级 code='03', 特级 code='00'
INSERT INTO `visit` (id, visit_no, patient_id, dept_id, dept_name, bed_no, admission_datetime, discharge_datetime, 
    nurse_level, nurse_level_code, patient_status, is_critical, doctor_id, doctor_name, sync_time, created_at, updated_at) VALUES
(1, 'V001', 1, 1, '心内科', 'A101', DATE_SUB(NOW(), INTERVAL 12 HOUR), NULL, '一级', '01', '在院', 1, 2, '李医生', NOW(), NOW(), NOW()),
(2, 'V002', 2, 1, '心内科', 'A102', DATE_SUB(NOW(), INTERVAL 48 HOUR), NULL, '一级', '01', '在院', 1, 2, '李医生', NOW(), NOW(), NOW()),
(3, 'V003', 3, 1, '心内科', 'A103', DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, '特级', '00', '在院', 1, 2, '李医生', NOW(), NOW(), NOW()),
(4, 'V004', 4, 1, '心内科', 'A104', DATE_SUB(NOW(), INTERVAL 72 HOUR), NULL, '二级', '02', '在院', 0, 2, '李医生', NOW(), NOW(), NOW()),
(5, 'V005', 5, 1, '心内科', 'A105', DATE_SUB(NOW(), INTERVAL 96 HOUR), NULL, '三级', '03', '在院', 0, 2, '李医生', NOW(), NOW(), NOW()),
(6, 'V006', 6, 2, '神经内科', 'B101', DATE_SUB(NOW(), INTERVAL 8 HOUR), NULL, '一级', '01', '在院', 1, 3, '王医生', NOW(), NOW(), NOW()),
(7, 'V007', 7, 2, '神经内科', 'B102', DATE_SUB(NOW(), INTERVAL 36 HOUR), NULL, '特级', '00', '在院', 1, 3, '王医生', NOW(), NOW(), NOW()),
(8, 'V008', 8, 2, '神经内科', 'B103', DATE_SUB(NOW(), INTERVAL 120 HOUR), NULL, '二级', '02', '在院', 0, 3, '王医生', NOW(), NOW(), NOW());

-- 插入临时医嘱数据 (用于测试 currentCondition)
INSERT INTO order_main (id, visit_id, visit_no, patient_id, patient_no, order_no, order_type, order_category, 
    doctor_id, doctor_name, start_time, end_time, status, sync_time, created_at) VALUES
(1, 1, 'V001', 1, 'P001', 'O001', 'N', '药物治疗', 2, '李医生', DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, '有效', NOW(), NOW()),
(2, 1, 'V001', 1, 'P001', 'O002', 'N', '检查', 2, '李医生', DATE_SUB(NOW(), INTERVAL 12 HOUR), NULL, '有效', NOW(), NOW()),
(3, 3, 'V003', 3, 'P003', 'O003', 'N', '紧急治疗', 2, '李医生', DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, '有效', NOW(), NOW()),
(4, 6, 'V006', 6, 'P006', 'O004', 'N', '康复治疗', 3, '王医生', DATE_SUB(NOW(), INTERVAL 8 HOUR), NULL, '有效', NOW(), NOW()),
(5, 6, 'V006', 6, 'P006', 'O005', 'Y', '长期用药', 3, '王医生', DATE_SUB(NOW(), INTERVAL 24 HOUR), NULL, '有效', NOW(), NOW());

SELECT '测试数据插入完成' AS message;