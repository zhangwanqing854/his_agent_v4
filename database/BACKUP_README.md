# 数据库备份说明

## 备份文件

- `backup_2026-04-16_handover_system_full.sql` - 完整数据库备份（结构与数据）

## 数据库配置

- 主机: localhost:3306
- 数据库名: handover_system
- 用户名: handover
- 密码: handover123

## 恢复步骤

1. 创建数据库用户（如果不存在）:
```sql
CREATE USER 'handover'@'localhost' IDENTIFIED BY 'handover123';
GRANT ALL PRIVILEGES ON handover_system.* TO 'handover'@'localhost';
FLUSH PRIVILEGES;
```

2. 恢复数据库:
```bash
mysql -u handover -phandover123 < backup_2026-04-16_handover_system_full.sql
```

## Schema 脚本顺序

如果从空数据库开始，按以下顺序执行 schema 脚本：

1. schema_v2.sql - 基础表结构
2. schema_v2.2_incremental_sync.sql - 增量同步
3. schema_v2.3.sql - 扩展字段
4. schema_v2.3_add_code_fields.sql - 编码字段
5. schema_v2.4_add_sync_fields.sql - 同步字段
6. schema_v2.5_interface_config_enhance.sql - 接口配置
7. schema_v2.6_visit_enhance.sql - 就诊扩展
8. schema_v2.7_sync_enhance.sql - 同步扩展
9. schema_v2.8_patient_enhance.sql - 患者扩展
10. schema_v2.9_dept_param.sql - 科室参数
11. schema_v2.10_sync_order.sql - 同步顺序
12. schema_v2.11_scheduling_tables.sql - 排班表
13. schema_v2.12_duty_staff.sql - 职责人员
14. interface_config_tables.sql - 接口配置表
15. test_data.sql - 测试数据（可选）

## 后端迁移脚本

后端 Flyway/Liquibase 风格迁移脚本位于:
- backend/src/main/resources/db/migration/
- backend/src/main/resources/db/sms_tables.sql
- backend/src/main/resources/schema/voice_session.sql