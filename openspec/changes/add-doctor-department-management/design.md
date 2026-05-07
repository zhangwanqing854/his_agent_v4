## 上下文

**现有情况：**
- doctor_department 表已存在，包含 id, doctor_id, department_id, is_primary, created_at 字段
- DoctorDepartment 实体类已定义
- DoctorDepartmentRepository 已存在
- 科室人员关系导入功能已实现（DoctorDepartmentImportService）

**数据关系：**
- doctor_id 关联 User 表（医护人员）
- department_id 关联 Department 表（科室）
- is_primary 标记是否为主科室（一个医生可能有多个科室，仅一个主科室）

## 目标 / 非目标

**目标：**
- 提供科室人员关系的 CRUD 维护界面
- 支持列表展示（分页、搜索）
- 支持新增、编辑、删除单条记录
- 支持设置主科室标记

**非目标：**
- 不实现批量操作
- 不实现导入功能（已有独立功能）
- 不修改现有导入功能

## 决策

### 1. API 设计

**RESTful API：**
- GET `/api/doctor-department-management` - 列表查询（分页）
- GET `/api/doctor-department-management/{id}` - 单条查询
- POST `/api/doctor-department-management` - 新增
- PUT `/api/doctor-department-management/{id}` - 编辑
- DELETE `/api/doctor-department-management/{id}` - 删除

**理由：** 标准 REST 设计，易于理解和维护。

### 2. 前端页面位置

**位置：** 系统设置页面新增 Tab "科室人员管理"

**理由：** 与科室信息、人员信息导入功能同属系统设置范畴，便于集中管理。

### 3. 数据展示

**列表字段：**
- 医护人员姓名（关联 User 表）
- 科室名称（关联 Department 表）
- 是否主科室
- 创建时间

**理由：** 显示关联信息便于用户确认关系正确性。

## 风险 / 权衡

- **删除风险：** 删除可能影响交接班数据 → 提供删除确认，记录操作日志
- **主科室唯一性：** 设置主科室时需确保同医生仅有一个主科室 → 后端校验并自动更新