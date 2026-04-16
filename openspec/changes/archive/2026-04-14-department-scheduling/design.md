## 上下文

系统已有科室管理（DepartmentManagement）和人员管理（HisStaffManagement）模块，科室人员数据可直接使用。排班功能需要在此基础上新增排班计划和排班明细的存储与管理。

**约束条件**：
- 仅支持当前登录科室的排班管理
- 排班周期为月度（按月为单位）
- 每天只有一个值班人员（不分班次）
- 打印格式固定为 A4 纸双月布局

## 目标 / 非目标

**目标：**
- 提供月度排班计划的创建、编辑、删除功能
- 支持从科室人员列表中选择每日值班人员
- 以卡片形式展示排班信息，便于快速浏览
- 实现卡片式打印，一张 A4 纸打印两个月排班
- 支持快速生成排班（基于人员排序自动轮换）

**非目标：**
- 不实现智能排班推荐算法
- 不支持跨科室排班或人员借调
- 不实现排班模板导入导出
- 不支持排班数据与 HIS 系统同步
- 不支持班次区分（白班/夜班）

## 决策

### 1. 数据模型设计

**排班主表 (scheduling)**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| department_id | BIGINT | 科室 ID |
| year_month | VARCHAR(7) | 排班月份（格式：YYYY-MM） |
| status | VARCHAR(10) | 状态（draft/published） |
| created_by | BIGINT | 创建人 ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**排班明细表 (scheduling_detail)**
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| scheduling_id | BIGINT | 排班主表 ID |
| duty_date | DATE | 值班日期 |
| staff_id | BIGINT | 值班人员 ID（关联 his_staff，可为空） |
| remark | VARCHAR(200) | 备注 |

**决策理由**：采用主从表结构，便于按月管理排班计划。每天只有一条明细记录，代表当天的值班人员，简化数据结构和用户操作。

**科室排班配置表 (department_scheduling_config)**
| 字段 | 类型 | 说明 |
|-----|------|------|
| department_id | BIGINT | 科室 ID（主键） |
| staff_order | JSON | 排班人员顺序，例如 `[5,3,2,4]` 表示人员ID顺序 |
| last_position | INT | 上次轮到索引（0-based），例如 2 表示上次轮到第3人 |
| updated_at | DATETIME | 更新时间 |

**决策理由**：科室级配置确保跨月连续轮换。`last_position` 记录上月月末位置，下月自动接续。

### 2. 前端组件架构

采用以下组件结构：
- `SchedulingManagement.vue` - 排班管理主页面（月份选择、列表展示）
- `SchedulingCard.vue` - 排班卡片组件（单月排班展示，按周排列）
- `SchedulingPrint.vue` - 打印模板组件（双月 A4 布局）
- `SchedulingEditDialog.vue` - 排班编辑对话框（日期点击编辑）

**决策理由**：卡片组件独立封装，便于复用和打印时组合使用。

### 3. 打印布局方案

一张 A4 纸（210mm × 297mm）打印两个月排班：
- 横向布局：左右各一个月卡片
- 卡片尺寸：约 95mm × 280mm
- 使用 CSS @media print 和 @page 规则控制打印样式

**替代方案**：纵向布局一个月 → 不采用，用户需求明确为双月打印。

### 4. API 设计

| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/scheduling | GET | 获取排班列表（按科室和月份筛选） |
| /api/scheduling/{id} | GET | 获取排班详情 |
| /api/scheduling | POST | 创建排班计划 |
| /api/scheduling/{id} | PUT | 更新排班状态（发布） |
| /api/scheduling/{id} | DELETE | 删除排班计划 |
| /api/scheduling/{id}/details | PUT | 批量更新排班明细 |
| /api/scheduling/{id}/auto-generate | POST | 快速生成排班 |
| /api/scheduling/config | GET | 获取科室排班配置 |
| /api/scheduling/config | PUT | 更新科室排班配置 |
| /api/scheduling/staff | GET | 获取当前科室可排班人员 |

### 5. 快速生成算法

**连续轮换逻辑**：
```
首次排班：
  起点 = 用户选择（例如第2位，索引 = 1）

非首次排班：
  起点 = (last_position + 1) % staff_order.length

每日轮换：
  day[i].staff_id = staff_order[(起点 + i) % staff_order.length]

生成完成后：
  last_position = (起点 + 天数 - 1) % staff_order.length
```

**决策理由**：索引方式灵活，人员离职时容易调整顺序。

### 6. 快速生成交互设计

**新增组件**：
- `SchedulingQuickGenerateDialog.vue` - 快速生成对话框（人员排序、日期范围、起点选择）

**交互流程**：
1. 创建空白排班 → 点击"快速生成"按钮
2. 弹出对话框：
   - 显示人员排序列表（可拖拽调整）
   - 开始日期、结束日期输入
   - 首次排班：显示起点选择（第N位开始）
   - 非首次：自动显示接续提示
3. 点击"生成" → 自动填充排班明细
4. 生成后可手动微调任意日期

**人员排序调整**：
- 支持拖拽排序或上下箭头调整
- 新增人员自动追加到排序末尾
- 离职人员需手动从排序移除

## 风险 / 权衡

| 风险 | 缓解措施 |
|-----|---------|
| 排班数据与 HIS 系统不一致 | 明确标注为非目标，不与 HIS 同步；如需同步需单独变更 |
| 打印样式在不同浏览器兼容性差 | 使用标准 CSS @media print，测试主流浏览器（Chrome、Edge、Firefox） |
| 月份切换时数据加载延迟 | 后端按月分页查询，前端缓存已加载月份数据 |

## 数据库迁移计划

1. 创建 scheduling 表和 scheduling_detail 表
2. 添加外键约束（关联 department、his_staff）
3. 无历史数据迁移（新功能）