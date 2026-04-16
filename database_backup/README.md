# 数据库备份目录

## 备份文件

| 文件名 | 大小 | 说明 |
|--------|------|------|
| handover_system_backup_20260416_155503.sql | 2.5MB | 完整备份，包含30个表 |

## 还原方法

```bash
# 使用最新备份还原
./restore_database.sh

# 使用指定备份文件还原
./restore_database.sh handover_system_backup_20260416_155503.sql
```

## 手动还原命令

```bash
# 删除并重建数据库
mysql -u handover -phandover123 -e "DROP DATABASE IF EXISTS handover_system;"
mysql -u handover -phandover123 -e "CREATE DATABASE handover_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入备份
mysql -u handover -phandover123 handover_system < handover_system_backup_20260416_155503.sql
```

## 创建新备份

```bash
mysqldump -u handover -phandover123 handover_system > handover_system_backup_$(date +%Y%m%d_%H%M%S).sql
```