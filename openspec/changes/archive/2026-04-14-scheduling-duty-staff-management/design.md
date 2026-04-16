## 上下文

当前科室排班功能的可排班人员来源为 `doctor_department` → `user` → `his_staff` 链路，这会将所有科室工作人员都纳入排班候选列表。但实际排班场景中，并非所有工作人员都需要参与值班（如行政岗、实习生、进修人员等）。

现有数据关系：
- `doctor_department.doctor_id` → `user.id` → `user.his_staff_id` → `his_staff.id`
- 科室 293（神经外科）约有 100+ 工作人员，但实际参与值班的仅约 20 人

## 目标 / 非目标

**目标：**
- 新增独立的科室值班人员配置功能，允许科室单独维护参与排班的人员名单
- 切换排班人员数据源，从值班人员表获取，不再从科室工作人员表自动获取
- 支持快速添加/移除值班人员

**非目标：**
- 不自动同步 HIS 系统值班人员数据
- 不实现值班人员资质校验（如职称要求）
- 不支持值班人员分组或班次类型分类

## 决策

### 1. 数据库设计

**决策：新增 `department_duty_staff` 表存储科室值班人员配置**

```sql
CREATE TABLE department_duty_staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL COMMENT '科室ID',
    staff_id BIGINT NOT NULL COMMENT '人员ID（关联 his_staff.id）',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_dept_staff (department_id, staff_id)
);
```

**理由：**
- 独立存储值班人员名单，与科室工作人员解耦
- `display_order` 支持人员排序，便于快速生成排班时使用
- `staff_id` 关联 `his_staff.id`，复用现有人员信息

**替代方案：**
- 方案 A：在 `his_staff` 表增加 `is_duty_staff` 标记 → 拒绝，因为同一人员可能在不同科室有不同值班安排
- 方案 B：复用 `doctor_department.is_primary` 字段 → 拒绝，该字段语义不同

### 2. API 设计

**新增接口：**

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/scheduling/duty-staff` | GET | 获取科室值班人员列表 |
| `/api/scheduling/duty-staff` | POST | 添加值班人员（批量） |
| `/api/scheduling/duty-staff/{staffId}` | DELETE | 移除值班人员 |
| `/api/scheduling/duty-staff/order` | PUT | 调整人员排序 |

**修改接口：**

| 接口 | 变更 |
|------|------|
| `/api/scheduling/staff` | 数据源从 `doctor_department` 改为 `department_duty_staff` |

### 3. 前端设计

**新增组件：**
- `DutyStaffConfigDialog.vue` - 值班人员配置对话框
  - 显示当前科室工作人员列表（从 `doctor_department` 获取）
  - 显示已配置的值班人员列表（从 `department_duty_staff` 获取）
  - 支持添加/移除操作
  - 支持拖拽排序

**集成位置：**
- 在 `SchedulingManagement.vue` 页面增加"配置值班人员"按钮入口
- 快速生成排班对话框（`SchedulingQuickGenerateDialog.vue`）人员列表来源变更

## 风险 / 权衡

| 风险 | 缓解措施 |
|------|----------|
| 科室首次使用时无值班人员配置，排班列表为空 | 提供初始化功能，一键导入所有科室工作人员为值班人员 |
| 值班人员与工作人员脱钩，人员变动后需手动更新配置 | 提供提醒机制，工作人员离职时提示更新值班人员配置 |
| 历史排班数据依赖旧的 doctor_department 数据源 | 保持向后兼容，若值班人员表为空则 fallback 到原有数据源 |

## 迁移计划

1. **Phase 1：新增表和接口**
   - 创建 `department_duty_staff` 表
   - 实现值班人员配置 CRUD 接口
   - 前端实现配置对话框

2. **Phase 2：数据源切换**
   - 修改 `SchedulingService.getSchedulableStaff()` 数据源
   - 实现 fallback 逻辑（值班人员表为空时使用原数据源）

3. **Phase 3：初始化支持**
   - 提供一键初始化功能，将现有科室工作人员导入值班人员表