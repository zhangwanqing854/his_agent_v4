## ADDED Requirements

### 需求:首页必须显示实时统计数据

首页必须从后端 API 获取并显示当前科室的统计数据，包括在院患者数、已完成交班数、待处理交班数。

#### 场景:登录后首页显示统计数据
- **当** 用户登录成功并进入首页
- **那么** 系统调用 `/api/dashboard/stats` API，根据当前科室 ID 返回统计数据

#### 场景:统计数据实时更新
- **当** 首页加载或刷新
- **那么** 系统从后端获取最新的统计数据，不在前端缓存

### 需求:在院患者数必须从 department_patient_overview 表获取

在院患者数必须从 department_patient_overview 表获取，使用当前科室的 total_num 字段值。

#### 场景:获取在院患者数成功
- **当** 系统请求在院患者数统计
- **那么** 返回 department_patient_overview 表中 `dept_id = 当前科室` 的 total_num 字段值

### 需求:已完成交班数必须从 shift_handover 表统计

已完成交班数必须从 shift_handover 表实时统计，仅计算当前科室状态为"COMPLETED"的交班记录。

#### 场景:统计已完成交班数成功
- **当** 系统请求已完成交班数统计
- **那么** 返回 shift_handover 表中 `dept_id = 当前科室 AND status = 'COMPLETED'` 的记录数

### 需求:待处理交班数必须从 shift_handover 表统计

待处理交班数必须从 shift_handover 表实时统计，仅计算当前科室状态为"PENDING"或"TRANSFERRING"的交班记录。

#### 场景:统计待处理交班数成功
- **当** 系统请求待处理交班数统计
- **那么** 返回 shift_handover 表中 `dept_id = 当前科室 AND status IN ('PENDING', 'TRANSFERRING')` 的记录数