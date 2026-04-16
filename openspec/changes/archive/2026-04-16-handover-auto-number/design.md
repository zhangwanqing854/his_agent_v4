## 上下文

当前交班记录存储在 `shift_handover` 表，使用自增 ID 作为主键。交班创建流程：用户发起交班 → 选择接班医生 → 调用 `createHandover` → 生成记录。

编号需求源于：
- 医疗文档需要唯一编号便于归档
- 跨系统对接需要唯一标识
- 审计追溯需要可读编号

## 目标 / 非目标

**目标：**
- 自动生成唯一交班编号
- 编码规则：年份后两位 + 月份 + 天 + 自增序号（如 `260415001`）
- 同一天内编号序号递增，跨天重置
- 编号全局唯一不可重复

**非目标：**
- 不支持自定义编号
- 不支持编号回收重用
- 不支持批量导入编号

## 决策

### 1. 编号格式

**选择：`YYMMDDSSS` 格式**

| 部分 | 示例 | 说明 |
|------|------|------|
| YY | 26 | 年份后两位 |
| MM | 04 | 月份（01-12） |
| DD | 15 | 日期（01-31） |
| SSS | 001 | 序号（001-999） |

**示例：** `260415001` = 2026年4月15日第1号

**理由：**
- 8位数字，便于记忆和输入
- 包含日期信息，可快速定位时间范围
- 序号3位足够（同一天999次交班足够）

**替代方案：**
- UUID → 无规律，不便于追溯
- 纯自增 → 无日期信息，难以定位
- 部门前缀 → 跨部门交班复杂

### 2. 序号生成策略

**选择：数据库锁 + 当日最大序号查询**

```java
public String generateHandoverNo() {
    LocalDate today = LocalDate.now();
    String prefix = formatDate(today); // "260415"
    
    // 查询当日最大序号（加锁）
    Long maxSeq = findMaxSequence(today);
    Long nextSeq = (maxSeq == null ? 0 : maxSeq) + 1;
    
    return prefix + String.format("%03d", nextSeq);
}
```

**理由：**
- 简单可靠，利用数据库事务保证唯一
- 无需额外组件（如 Redis）
- 序号按日期分区，自然重置

**替代方案：**
- Redis 分布式序号 → 增加依赖，复杂度高
- 单独序号表 → 需额外维护

### 3. 并发控制

**选择：数据库唯一索引 + 重试机制**

```sql
ALTER TABLE shift_handover ADD COLUMN handover_no VARCHAR(10) UNIQUE;
```

**理由：**
- 唯一索引保证数据库层面不重复
- 插入失败时重试生成新编号
- 无需分布式锁

**流程：**
1. 生成编号
2. 尝试插入
3. 唯一约束冲突 → 重新生成 → 重试（最多3次）

### 4. 字段设计

**`shift_handover` 表添加：**

```sql
ALTER TABLE shift_handover 
ADD COLUMN handover_no VARCHAR(10) UNIQUE COMMENT '交班编号';
```

**理由：**
- VARCHAR(10) 足够存储 8位编号
- UNIQUE 约束保证唯一性
- 可为空（兼容历史数据）

## 风险 / 权衡

### 风险：高并发冲突
→ **缓解：** 唯一索引 + 重试机制，最多重试3次

### 风险：序号耗尽（单日超999次）
→ **缓解：** 扩展序号位数（改为4位 SSSS），或日志预警

### 风险：历史数据无编号
→ **缓解：** 允许 handover_no 为 NULL，新记录自动生成

### 风险：跨年边界（如 2026→2027）
→ **缓解：** 使用年份后两位自然切换

## 迁移计划

### 部署步骤

1. 执行 SQL 添加 `handover_no` 列（允许 NULL）
2. 部署后端代码（编号生成服务）
3. 新交班自动生成编号
4. 历史交班保持 NULL（可选批量补充）

### SQL 脚本

```sql
ALTER TABLE shift_handover 
ADD COLUMN handover_no VARCHAR(10) UNIQUE COMMENT '交班编号';
```

### 回滚策略

```sql
ALTER TABLE shift_handover DROP COLUMN handover_no;
```

删除编号生成服务代码。

## 开放问题

1. **历史数据补编号**：是否需要批量生成历史交班编号？（建议：不强制，新记录自动生成）
2. **编号显示位置**：前端列表和详情页的编号显示位置？（建议：列表标题、详情头部）