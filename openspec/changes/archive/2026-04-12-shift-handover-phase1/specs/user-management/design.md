# 用户管理模块设计文档

## 1. 概述

### 1.1 背景

系统需要支持用户管理功能，包括：
- 用户账号的增删改查
- 权限控制（基于RBAC模型）
- 与HIS系统同步的医生信息自动关联

### 1.2 设计目标

- 实现RBAC（基于角色的访问控制）权限模型
- 支持超级管理员和普通医生两种预置角色
- HIS同步医生信息时自动创建系统账号

---

## 2. 数据模型设计

### 2.1 ER图

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           用户权限模型（RBAC）                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│   ┌────────────────┐         ┌────────────────┐         ┌────────────────┐         │
│   │      用户       │         │      角色       │         │      权限       │         │
│   │     user       │         │     role       │         │  permission    │         │
│   ├────────────────┤         ├────────────────┤         ├────────────────┤         │
│   │ id             │         │ id             │         │ id             │         │
│   │ username       │         │ name           │         │ code           │         │
│   │ password       │         │ code           │         │ name           │         │
│   │ is_super_admin │         │ is_default     │         │ description    │         │
│   │ his_staff_id   │         │ description    │         └───────┬────────┘         │
│   │ role_id        │         └───────┬────────┘                 │                  │
│   │ enabled        │                 │                          │                  │
│   │ created_at     │                 │                          │                  │
│   │ updated_at     │                 │                          │                  │
│   └───────┬────────┘                 │                          │                  │
│           │                          │                          │                  │
│           │    ┌─────────────────────┴──────────────────────────┘                  │
│           │    │                                                                     │
│           ▼    ▼                                                                     │
│   ┌────────────────┐         ┌────────────────┐                                     │
│   │  role_permission│         │      职责       │                                     │
│   │   (关联表)      │         │     duty       │                                     │
│   ├────────────────┤         ├────────────────┤                                     │
│   │ role_id        │         │ id             │                                     │
│   │ permission_id  │         │ code           │                                     │
│   └────────────────┘         │ name           │                                     │
│                              │ description    │                                     │
│                              └───────┬────────┘                                     │
│                                      │                                              │
│                                      ▼                                              │
│                              ┌────────────────┐                                     │
│                              │     功能点      │                                     │
│                              │    function    │                                     │
│                              ├────────────────┤                                     │
│                              │ code           │                                     │
│                              │ name           │                                     │
│                              │ route          │                                     │
│                              └────────────────┘                                     │
│                                                                                     │
│   ┌────────────────┐                                                               │
│   │   HIS医生信息   │                                                               │
│   │   his_staff    │                                                               │
│   ├────────────────┤                                                               │
│   │ id             │                                                               │
│   │ staff_code     │◀─────────── 用户.his_staff_id 关联                            │
│   │ name           │                                                               │
│   │ department_id  │                                                               │
│   │ title          │                                                               │
│   │ sync_time      │                                                               │
│   └────────────────┘                                                               │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 表结构定义

#### 2.2.1 用户表 (user)

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | 是 | - | 主键，自增 |
| username | VARCHAR(50) | 是 | - | 用户名，唯一 |
| password | VARCHAR(255) | 是 | - | 密码（加密存储） |
| is_super_admin | TINYINT(1) | 是 | 0 | 是否超级管理员 |
| his_staff_id | BIGINT | 否 | NULL | 关联HIS医生ID |
| role_id | BIGINT | 是 | - | 角色ID |
| enabled | TINYINT(1) | 是 | 1 | 是否启用 |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | 是 | CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_username (username)
- KEY idx_his_staff_id (his_staff_id)
- KEY idx_role_id (role_id)

#### 2.2.2 角色表 (role)

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | 是 | - | 主键，自增 |
| name | VARCHAR(50) | 是 | - | 角色名称 |
| code | VARCHAR(50) | 是 | - | 角色编码，唯一 |
| is_default | TINYINT(1) | 是 | 0 | 是否默认角色 |
| description | VARCHAR(255) | 否 | NULL | 角色描述 |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | 是 | CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_code (code)

#### 2.2.3 权限表 (permission)

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | 是 | - | 主键，自增 |
| code | VARCHAR(50) | 是 | - | 权限编码，唯一 |
| name | VARCHAR(50) | 是 | - | 权限名称 |
| description | VARCHAR(255) | 否 | NULL | 权限描述 |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_code (code)

#### 2.2.4 角色-权限关联表 (role_permission)

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | 是 | - | 主键，自增 |
| role_id | BIGINT | 是 | - | 角色ID |
| permission_id | BIGINT | 是 | - | 权限ID |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_role_permission (role_id, permission_id)
- KEY idx_permission_id (permission_id)

#### 2.2.5 HIS医生信息表 (his_staff)

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | 是 | - | 主键，自增 |
| staff_code | VARCHAR(50) | 是 | - | 工号，唯一 |
| name | VARCHAR(50) | 是 | - | 姓名 |
| department_id | BIGINT | 是 | - | 科室ID |
| title | VARCHAR(50) | 否 | NULL | 职称 |
| phone | VARCHAR(20) | 否 | NULL | 电话 |
| sync_time | DATETIME | 是 | - | 最后同步时间 |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | 是 | CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_staff_code (staff_code)
- KEY idx_department_id (department_id)

### 2.3 表关系说明

```
user.role_id ──────────▶ role.id
user.his_staff_id ─────▶ his_staff.id

role_permission.role_id ─────▶ role.id
role_permission.permission_id ─▶ permission.id

his_staff.department_id ──────▶ department.id
```

---

## 3. 预置数据

### 3.1 角色预置

| id | name | code | is_default | description |
|----|------|------|------------|-------------|
| 1 | 超级管理员 | SUPER_ADMIN | 0 | 系统超级管理员，拥有所有权限 |
| 2 | 普通医生 | DOCTOR | 1 | 普通医生角色，HIS同步医生的默认角色 |

### 3.2 权限预置

| id | code | name | description |
|----|------|------|-------------|
| 1 | HANDOVER | 交班管理 | 交班记录的创建、查看、编辑、确认 |
| 2 | PATIENT | 患者管理 | 患者信息查看、详情查看 |
| 3 | TODO | 待办事项 | 待办事项的查看、处理 |
| 4 | STATISTICS | 统计分析 | 统计数据查看 |
| 5 | USER_MANAGE | 用户管理 | 用户的增删改查、权限分配 |
| 6 | SYSTEM_SETTINGS | 系统设置 | 系统配置管理 |

### 3.3 角色权限预置

**超级管理员：** 拥有全部权限 (1-6)

**普通医生：** 拥有以下权限
- 交班管理 (1)
- 患者管理 (2)
- 待办事项 (3)
- 统计分析 (4)

### 3.4 初始管理员用户

| 字段 | 值 |
|------|-----|
| username | admin |
| password | admin（加密后存储） |
| is_super_admin | 1 |
| his_staff_id | NULL |
| role_id | 1（超级管理员） |
| enabled | 1 |

---

## 4. 业务规则

### 4.1 用户创建规则

| 场景 | 规则 |
|------|------|
| 管理员手动创建 | 填写用户名、密码、选择角色、可选关联HIS医生 |
| HIS同步新医生 | 自动创建用户，用户名=工号，密码=工号，角色=普通医生 |
| 用户名重复 | 不允许创建，提示用户名已存在 |

### 4.2 登录规则

| 规则 | 说明 |
|------|------|
| 用户名密码验证 | 校验用户名和密码是否匹配 |
| 启用状态检查 | enabled=0 的用户不允许登录 |
| 登录成功 | 生成JWT Token，返回用户信息和权限列表 |

### 4.3 权限校验规则

| 规则 | 说明 |
|------|------|
| 超级管理员 | is_super_admin=1 时，跳过所有权限检查，直接放行 |
| 普通用户 | 根据用户的 role_id 查询 role_permission 表，判断是否有对应权限 |
| 权限缓存 | 用户权限信息缓存到Redis，更新权限时清除缓存 |

### 4.4 用户管理权限

| 操作 | 权限要求 |
|------|---------|
| 查看用户列表 | 需要 USER_MANAGE 权限 或 is_super_admin=1 |
| 新增用户 | 需要 USER_MANAGE 权限 或 is_super_admin=1 |
| 编辑用户 | 需要 USER_MANAGE 权限 或 is_super_admin=1 |
| 删除用户 | 需要 USER_MANAGE 权限 或 is_super_admin=1 |
| 启用/禁用用户 | 需要 USER_MANAGE 权限 或 is_super_admin=1 |
| 重置密码 | 需要 USER_MANAGE 权限 或 is_super_admin=1 |
| 分配角色 | 需要 USER_MANAGE 权限 或 is_super_admin=1 |

### 4.5 保护规则

| 规则 | 说明 |
|------|------|
| 超级管理员账号不可删除 | username='admin' 且 is_super_admin=1 的账号不允许删除 |
| 超级管理员账号不可禁用 | username='admin' 且 is_super_admin=1 的账号不允许禁用 |
| 不能删除自己 | 当前登录用户不能删除自己的账号 |
| 不能禁用自己 | 当前登录用户不能禁用自己的账号 |

---

## 5. API设计

### 5.1 用户管理API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/users | 获取用户列表 | USER_MANAGE |
| GET | /api/users/{id} | 获取用户详情 | USER_MANAGE |
| POST | /api/users | 创建用户 | USER_MANAGE |
| PUT | /api/users/{id} | 更新用户 | USER_MANAGE |
| DELETE | /api/users/{id} | 删除用户 | USER_MANAGE |
| PUT | /api/users/{id}/enable | 启用用户 | USER_MANAGE |
| PUT | /api/users/{id}/disable | 禁用用户 | USER_MANAGE |
| PUT | /api/users/{id}/reset-password | 重置密码 | USER_MANAGE |

### 5.2 角色管理API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/roles | 获取角色列表 | USER_MANAGE |
| GET | /api/roles/{id} | 获取角色详情 | USER_MANAGE |
| POST | /api/roles | 创建角色 | USER_MANAGE |
| PUT | /api/roles/{id} | 更新角色 | USER_MANAGE |
| DELETE | /api/roles/{id} | 删除角色 | USER_MANAGE |

### 5.3 权限管理API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/permissions | 获取权限列表 | USER_MANAGE |
| PUT | /api/roles/{id}/permissions | 更新角色权限 | USER_MANAGE |

### 5.4 HIS医生同步API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/his-staff | 获取HIS医生列表 | USER_MANAGE |
| POST | /api/his-staff/sync | 同步HIS医生信息 | SYSTEM_SETTINGS |

---

## 6. 前端界面设计

### 6.1 用户管理页面

**位置：** 系统设置 → 用户管理 Tab

**功能：**
- 用户列表（表格形式）
- 筛选：用户名、角色、状态
- 操作：新增、编辑、删除、启用/禁用、重置密码
- 分页

**列表字段：**
| 字段 | 说明 |
|------|------|
| 用户名 | username |
| 姓名 | 关联 his_staff.name 或直接显示 |
| 角色 | role.name |
| 关联医生 | his_staff.name |
| 状态 | enabled（启用/禁用） |
| 创建时间 | created_at |
| 操作 | 编辑、删除、启用/禁用、重置密码 |

### 6.2 用户编辑弹窗

**字段：**
- 用户名（username）
- 密码（password，新建时必填，编辑时可选）
- 角色（role_id，下拉选择）
- 关联HIS医生（his_staff_id，下拉选择）
- 状态（enabled，开关）

---

## 7. 安全设计

### 7.1 密码安全

| 规则 | 说明 |
|------|------|
| 加密存储 | 使用 BCrypt 加密 |
| 初始密码 | HIS同步医生的初始密码为工号 |
| 强制修改 | 首次登录强制修改密码（可选） |

### 7.2 Token管理

| 规则 | 说明 |
|------|------|
| Token生成 | JWT格式，包含用户ID、用户名、角色ID |
| Token过期 | 24小时过期 |
| Token刷新 | 过期前可刷新 |

### 7.3 操作日志

| 操作 | 记录内容 |
|------|---------|
| 用户登录 | 用户名、登录时间、IP地址 |
| 用户创建 | 操作人、创建的用户信息 |
| 用户修改 | 操作人、修改前后的信息 |
| 密码重置 | 操作人、被重置的用户 |

---

## 8. 扩展性设计

### 8.1 未来可扩展

| 扩展点 | 说明 |
|------|------|
| 更多角色 | 可新增科室主任、护士长等角色 |
| 细粒度权限 | 可将权限细分为查看、新增、编辑、删除等 |
| 数据权限 | 可扩展为科室级数据权限 |
| 审批流程 | 可扩展用户创建审批流程 |

### 8.2 配置化

| 配置项 | 说明 |
|------|------|
| 密码强度要求 | 可配置密码复杂度要求 |
| 登录失败锁定 | 可配置登录失败次数限制 |
| Token过期时间 | 可配置Token有效期 |