#!/bin/bash
# 数据库还原脚本
# 使用方法: ./restore_database.sh [备份文件名]

BACKUP_DIR="/Users/admin/workspace/his_agent_v4/database_backup"
DB_USER="handover"
DB_PASS="handover123"
DB_NAME="handover_system"

# 如果没有指定备份文件，使用最新的备份
if [ -z "$1" ]; then
    BACKUP_FILE=$(ls -t ${BACKUP_DIR}/handover_system_backup_*.sql | head -1)
    echo "使用最新备份文件: $BACKUP_FILE"
else
    BACKUP_FILE="${BACKUP_DIR}/$1"
fi

if [ ! -f "$BACKUP_FILE" ]; then
    echo "错误: 备份文件不存在: $BACKUP_FILE"
    exit 1
fi

echo "======================================"
echo "数据库还原脚本"
echo "======================================"
echo "备份文件: $BACKUP_FILE"
echo "目标数据库: $DB_NAME"
echo "文件大小: $(ls -lh $BACKUP_FILE | awk '{print $5}')"
echo ""

# 确认还原
read -p "确认还原数据库? 这将覆盖现有数据! (y/n): " confirm
if [ "$confirm" != "y" ]; then
    echo "已取消还原"
    exit 0
fi

echo ""
echo "开始还原..."

# 先删除数据库再导入（确保干净还原）
mysql -u $DB_USER -p$DB_PASS -e "DROP DATABASE IF EXISTS $DB_NAME;" 2>&1
mysql -u $DB_USER -p$DB_PASS -e "CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>&1

# 导入备份
mysql -u $DB_USER -p$DB_PASS $DB_NAME < $BACKUP_FILE 2>&1

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "还原成功!"
    echo "======================================"
    
    # 显示还原后的表数量
    TABLE_COUNT=$(mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "SHOW TABLES;" 2>&1 | tail -n +2 | wc -l | tr -d ' ')
    echo "已还原表数量: $TABLE_COUNT"
else
    echo ""
    echo "还原失败，请检查错误信息"
fi