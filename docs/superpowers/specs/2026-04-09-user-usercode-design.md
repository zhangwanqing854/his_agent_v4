# User 表增加 usercode 用户编码设计文档

**日期**: 2026-04-09
**状态**: 待用户审核

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

// loginApi 函数签名
export async function loginApi(params: LoginParams): Promise<ApiResponse<LoginResponse>> {
  if (USE_MOCK) {
    // mock 登录逻辑（不区分大小写）
    const user = mockUsers.find(u =>
      u.usercode.toLowerCase() === params.usercode.toLowerCase() &&
      u.password === params.password
    )
    // ...
  }
  return request.post('/auth/login', params)
}
```

### Auth Store (`stores/auth.ts`)

```typescript
// login 方法签名变更
async login(usercode: string, password: string): Promise<boolean> {
  const response = await loginApi({ usercode, password })
  // ...
}
```

### 登录验证规则

- 登录时 usercode 不区分大小写匹配
- 示例：输入 "ADMIN" 或 "admin" 均可登录 usercode 为 "admin" 的账户

### Mock 登录逻辑

```typescript
// mock/auth.ts 登录验证
const user = mockUsers.find(u => 
  u.usercode.toLowerCase() === params.usercode.toLowerCase() &&
  u.password === params.password
)
```

### 后端校验逻辑（待实现）

- 登录接口：`WHERE LOWER(usercode) = LOWER(input_usercode)`
- 创建/更新接口：校验 `LOWER(usercode)` 是否已存在

---

## 界面变更

### 登录页面 (`views/Login.vue`)

```typescript
// loginForm 定义
const loginForm = reactive({
  usercode: '',   // 改：原为 username
  password: ''
})

// 输入框绑定
<el-input v-model="loginForm.usercode" placeholder="请输入用户编码" />
<el-input v-model="loginForm.password" type="password" />

// 提交调用
const handleLogin = async () => {
  await authStore.login(loginForm.usercode, loginForm.password)
}
```

- 输入框 label/placeholder 改为"用户编码"
- 错误提示：`ElMessage.error('用户编码或密码错误')`（原为"账号或密码错误"）

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
- HIS 系统返回的 `code_user` 字段 → `user.usercode`
- 现有 HisStaff 接口使用 `staffCode`，同步时需确认 HIS API 字段名映射

**注意**：当前 HisStaff.staffCode 可能需改为 code_user，或同步时直接映射：
```typescript
// HIS 同步时的映射逻辑
user.usercode = hisStaffData.code_user || hisStaffData.staffCode
```

---

## 文件变更清单

| 文件 | 变更内容 |
|------|----------|
| `types/user.ts` | User、UserFormData 接口添加 usercode 字段 |
| `api/auth.ts` | LoginParams 接口：username → usercode；login 函数参数调整 |
| `stores/auth.ts` | login 方法参数接收 usercode，传递给 API |
| `mock/user.ts` | mockUsers 数组添加 usercode 字段；新增 checkUsercodeExists 函数 |
| `api/user.ts` | createUser、updateUser 函数使用更新后的 UserFormData（含 usercode） |
| `views/Login.vue` | 输入框 label 改"用户编码"，loginForm 字段名改 usercode |
| `views/UserManagement.vue` | 表格新增"用户编码"列 |
| `components/settings/UserEditDialog.vue` | 新增 usercode 输入框 + 必填校验 + 唯一性校验函数调用 |

### 校验函数详情

新增 `checkUsercodeExists(usercode: string, excludeId?: number)` 函数：

```typescript
// mock/user.ts 或 api/user.ts
export function checkUsercodeExists(usercode: string, excludeId?: number): boolean {
  // 不区分大小写比较
  const normalized = usercode.toLowerCase()
  return mockUsers.some(u => 
    u.usercode.toLowerCase() === normalized && u.id !== excludeId
  )
}
```

- `excludeId`: 编辑时排除当前用户自身，避免误判

---

## 校验规则

| 规则 | 说明 |
|------|------|
| usercode 必填 | 创建/编辑用户时必须填写 |
| usercode 唯一性 | 不区分大小写，如已存在 "Admin"，不允许录入 "ADMIN" 或 "admin" |
| 登录匹配不区分大小写 | 输入值与数据库值进行 LOWER() 比较 |

---

## 数据迁移策略

### 现有数据处理

数据库迁移时需处理现有用户的 usercode 值：

```sql
-- 1. 添加字段（允许临时 NULL）
ALTER TABLE user ADD COLUMN usercode VARCHAR(50);

-- 2. 为现有用户填充 usercode（从 username 或 hisStaff.staffCode）
UPDATE user SET usercode = LOWER(username) WHERE usercode IS NULL AND his_staff_id IS NULL;
UPDATE user u SET usercode = LOWER(hs.staff_code) 
FROM his_staff hs WHERE u.his_staff_id = hs.id AND u.usercode IS NULL;

-- 3. 添加唯一约束（NOT NULL + UNIQUE）
ALTER TABLE user MODIFY COLUMN usercode VARCHAR(50) NOT NULL;
ALTER TABLE user ADD CONSTRAINT uk_user_usercode UNIQUE (usercode);
```

### 现有 Mock 数据填充

为 5 个现有 mock 用户补充 usercode：
- admin → usercode: "admin"（从 username 复制）
- D001 用户 → usercode: "d001"（从 hisStaff.staffCode）
- D002 用户 → usercode: "d002"
- D003 用户 → usercode: "d003"
- sysadmin → usercode: "sysadmin"

---

## 后端待实现（Spring Boot）

1. User 实体添加 usercode 字段
2. 数据库迁移：按上述策略执行
3. 登录接口改为 usercode 验证（LOWER 比较）
4. 创建/更新接口添加 usercode 唯一性校验（LOWER 比较）
5. HIS 同步接口：code_user 映射到 usercode

---

## 测试要点

### 功能测试

1. **新用户创建**
   - usercode 为空时，提示"请输入用户编码"
   - usercode 唯一性校验生效

2. **usercode 重复校验（不区分大小写）**
   - 已存在 usercode="Admin"，新建输入 "admin" → 提示"用户编码已存在"
   - 已存在 usercode="Admin"，新建输入 "ADMIN" → 提示"用户编码已存在"
   - 编辑时排除自身：编辑 usercode="admin" 的用户，输入 "admin" → 允许（排除自身）

3. **登录功能**
   - 输入 "admin"/正确密码 → 登录成功
   - 输入 "ADMIN"/正确密码 → 登录成功
   - 输入 "Admin"/正确密码 → 登录成功
   - 输入 "wrongcode"/正确密码 → 提示"用户编码或密码错误"

4. **HIS 同步**
   - HIS 返回 `{ code_user: "HIS001", name: "张三" }` → 新建用户 usercode="his001"
   - 同步已存在用户时更新 usercode

5. **用户列表**
   - usercode 列正确显示
   - 列顺序正确：用户编码 → 昵称 → 角色...

6. **用户编辑**
   - usercode 字段可修改
   - 唯一性校验生效

### 边界测试

- usercode 长度限制（建议 2-50 字符）
- usercode 格式（允许字母、数字、下划线）
- 空字符串校验