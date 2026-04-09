# User 表增加 usercode 用户编码设计文档

**日期**: 2026-04-09
**状态**: 待审核

---

## 需求概述

为 User 表增加 `usercode` 用户编码字段，用于系统登录。现有 `username` 字段改为显示名称（昵称）用途。

### 核心需求

1. `usercode` 用于登录，`username` 用于显示昵称
2. 支持手动录入 usercode
3. HIS 同步时，`hisStaff.code_user` 映射到 `user.usercode`
4. usercode 必须唯一，需重复校验
5. 登录匹配、唯一性校验均不区分大小写

---

## 设计方案

采用最小改动方案：仅添加 usercode 字段，调整登录逻辑，不改变现有字段含义。

---

## 数据结构变更

### User 接口 (`types/user.ts`)

```typescript
export interface User {
  id: number
  usercode: string           // 新增：登录用户编码，唯一
  username: string           // 保留：显示名称/昵称
  password?: string
  isSuperAdmin: boolean
  hisStaffId: number | null
  roleId: number
  enabled: boolean
  createdAt: string
  updatedAt: string
  hisStaff?: HisStaff
  role?: Role
}
```

### UserFormData 接口 (`types/user.ts`)

```typescript
export interface UserFormData {
  usercode: string           // 新增
  username: string
  password?: string
  roleId: number
  hisStaffId: number | null
  enabled: boolean
}
```

---

## 登录逻辑变更

### LoginParams (`api/auth.ts`)

```typescript
export interface LoginParams {
  usercode: string    // 改：原为 username
  password: string
}
```

### 登录验证规则

- 登录时 usercode 不区分大小写匹配
- 示例：输入 "ADMIN" 或 "admin" 均可登录 usercode 为 "admin" 的账户

### 后端校验逻辑（待实现）

- 登录接口：`WHERE LOWER(usercode) = LOWER(input_usercode)`
- 创建/更新接口：校验 `LOWER(usercode)` 是否已存在

---

## 界面变更

### 登录页面 (`views/Login.vue`)

- 输入框 label 改为"用户编码"
- 提交参数使用 `usercode`

### 用户管理列表 (`views/UserManagement.vue`)

- 表格新增"用户编码"列，显示 usercode
- 列顺序：用户编码 → 昵称 → 角色 → HIS人员 → 状态 → 创建时间

### 用户编辑对话框 (`components/settings/UserEditDialog.vue`)

- 新增 usercode 输入框
- 表单字段顺序：用户编码 → 昵称 → 密码 → 角色 → HIS人员 → 启用状态
- usercode 必填校验
- usercode 唯一性校验（不区分大小写）

---

## Mock 数据变更 (`mock/user.ts`)

为 5 个现有用户添加 usercode 字段：

| 原用户 | usercode |
|--------|----------|
| admin | "admin" |
| D001 用户 | "d001" |
| D002 用户 | "d002" |
| D003 用户 | "d003" |
| sysadmin | "sysadmin" |

---

## HIS 同步逻辑

从 HIS 同步人员信息时：
- `hisStaff.code_user` → `user.usercode`

---

## 文件变更清单

| 文件 | 变更内容 |
|------|----------|
| `types/user.ts` | User、UserFormData 接口添加 usercode 字段 |
| `api/auth.ts` | LoginParams.username → usercode |
| `stores/auth.ts` | 无需变更 |
| `mock/user.ts` | 5 个用户添加 usercode 值 |
| `api/user.ts` | 无需变更（类型已更新） |
| `views/Login.vue` | 输入框 label 改"用户编码"，参数改 usercode |
| `views/UserManagement.vue` | 表格新增"用户编码"列 |
| `components/settings/UserEditDialog.vue` | 新增 usercode 输入框 + 唯一性校验 |

---

## 校验规则

| 规则 | 说明 |
|------|------|
| usercode 必填 | 创建/编辑用户时必须填写 |
| usercode 唯一性 | 不区分大小写，如已存在 "Admin"，不允许录入 "ADMIN" 或 "admin" |
| 登录匹配不区分大小写 | 输入值与数据库值进行 LOWER() 比较 |

---

## 后端待实现（Spring Boot）

1. User 实体添加 usercode 字段
2. 数据库迁移：ALTER TABLE user ADD COLUMN usercode VARCHAR(50) UNIQUE
3. 登录接口改为 usercode 验证（LOWER 比较）
4. 创建/更新接口添加 usercode 唯一性校验（LOWER 比较）
5. HIS 同步接口：code_user 映射到 usercode

---

## 测试要点

1. 新用户创建：usercode 必填校验
2. usercode 重复校验：不区分大小写
3. 登录功能：usercode 登录，不区分大小写
4. HIS 同步：code_user 正确映射到 usercode
5. 用户列表：usercode 列正确显示
6. 用户编辑：usercode 字段可修改且校验生效