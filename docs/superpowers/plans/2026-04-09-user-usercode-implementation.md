# User 表增加 usercode 用户编码实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 User 表添加 usercode 用户编码字段，用于系统登录，实现不区分大小写的唯一性校验和登录匹配。

**Architecture:** 最小改动方案：添加 usercode 字段，调整登录逻辑使用 usercode，保持 username 作为显示昵称。改动集中在 8 个文件。

**Tech Stack:** Vue 3 + TypeScript + Vite, Element Plus UI, Pinia Store, Mock Data

---

## 重要说明

**当前代码状态**：所有目标文件均**不包含** usercode 相关代码，需要**新增**。

**执行顺序**：
1. 每个步骤展示**要添加的代码**
2. 验证命令在代码添加**之后**执行
3. 验证期望的是**添加后**的状态

**依赖顺序**：
- Task 1 → Task 2 → Task 3 → ...（按顺序执行）
- Task 5 的导入依赖 Task 2 创建的函数

---

## Chunk 1: 类型定义和 Mock 数据更新

### Task 1: 更新 User 和 UserFormData 类型定义

**Files:**
- Modify: `frontend/src/types/user.ts`

- [ ] **Step 1: 在 User 接口添加 usercode 字段**

在 `User` 接口中添加 `usercode` 字段，位于 `id` 之后：

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

- [ ] **Step 2: 在 UserFormData 接口添加 usercode 字段**

在 `UserFormData` 接口中添加 `usercode` 字段：

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

- [ ] **Step 3: 运行类型检查验证（在 Step 1-2 完成后）**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "types/user.ts"`
Expected: 无错误输出（usercode 字段已添加，类型定义正确）

- [ ] **Step 4: 提交类型变更**

```bash
git add frontend/src/types/user.ts
git commit -m "feat(types): add usercode field to User and UserFormData interfaces"
```

---

### Task 2: 更新 Mock 用户数据

**Files:**
- Modify: `frontend/src/mock/user.ts`

- [ ] **Step 1: 为所有 mockUsers 添加 usercode 字段**

在 `mockUsers` 数组中为每个用户添加 `usercode` 字段：

```typescript
export const mockUsers: User[] = [
  {
    id: 1,
    usercode: 'admin',        // 新增
    username: 'admin',
    password: 'admin123',
    isSuperAdmin: true,
    hisStaffId: null,
    roleId: 1,
    enabled: true,
    createdAt: '2024-01-01 00:00:00',
    updatedAt: '2024-01-01 00:00:00',
    role: mockRoles.find(r => r.id === 1)
  },
  {
    id: 2,
    usercode: 'd001',         // 新增
    username: '张医生',
    password: 'd001123',
    isSuperAdmin: false,
    hisStaffId: 1,
    roleId: 2,
    enabled: true,
    createdAt: '2024-01-02 00:00:00',
    updatedAt: '2024-01-02 00:00:00',
    hisStaff: mockHisStaff.find(s => s.id === 1),
    role: mockRoles.find(r => r.id === 2)
  },
  {
    id: 3,
    usercode: 'd002',         // 新增
    username: '李医生',
    password: 'd002123',
    isSuperAdmin: false,
    hisStaffId: 2,
    roleId: 2,
    enabled: true,
    createdAt: '2024-01-03 00:00:00',
    updatedAt: '2024-01-03 00:00:00',
    hisStaff: mockHisStaff.find(s => s.id === 2),
    role: mockRoles.find(r => r.id === 2)
  },
  {
    id: 4,
    usercode: 'd003',         // 新增
    username: '王主任',
    password: 'd003123',
    isSuperAdmin: false,
    hisStaffId: 3,
    roleId: 3,
    enabled: true,
    createdAt: '2024-01-04 00:00:00',
    updatedAt: '2024-01-04 00:00:00',
    hisStaff: mockHisStaff.find(s => s.id === 3),
    role: mockRoles.find(r => r.id === 3)
  },
  {
    id: 5,
    usercode: 'sysadmin',     // 新增
    username: '系统管理员',
    password: 'sysadmin123',
    isSuperAdmin: false,
    hisStaffId: null,
    roleId: 4,
    enabled: true,
    createdAt: '2024-01-05 00:00:00',
    updatedAt: '2024-01-05 00:00:00',
    role: mockRoles.find(r => r.id === 4)
  }
]
```

- [ ] **Step 2: 添加 checkUsercodeExists 函数**

在文件末尾添加唯一性校验函数：

```typescript
/**
 * 检查用户编码是否已存在（不区分大小写）
 * @param usercode 要检查的用户编码
 * @param excludeId 排除的用户ID（编辑时排除自身）
 * @returns 是否存在
 */
export function checkUsercodeExists(usercode: string, excludeId?: number): boolean {
  const normalized = usercode.toLowerCase()
  return mockUsers.some(u => 
    u.usercode.toLowerCase() === normalized && u.id !== excludeId
  )
}
```

- [ ] **Step 3: 运行类型检查验证（在 Step 1-2 完成后）**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "mock/user.ts"`
Expected: 无错误输出（usercode 字段和 checkUsercodeExists 函数已添加）

- [ ] **Step 4: 提交 Mock 数据变更**

```bash
git add frontend/src/mock/user.ts
git commit -m "feat(mock): add usercode to mockUsers and checkUsercodeExists function"
```

---

## Chunk 2: API 层和 Auth Store 更新

### Task 3: 更新登录 API 接口

**Files:**
- Modify: `frontend/src/api/auth.ts`

- [ ] **Step 1: 更新 LoginParams 接口**

将 `username` 改为 `usercode`：

```typescript
export interface LoginParams {
  usercode: string    // 改：原为 username
  password: string
}
```

- [ ] **Step 2: 更新 loginApi 函数（添加 mock 登录逻辑）**

当前文件直接调用 `request.post`，需**添加** mock 登录逻辑。使用 usercode 进行不区分大小写的匹配：

```typescript
export async function loginApi(params: LoginParams): Promise<ApiResponse<LoginResponse>> {
  if (USE_MOCK) {
    // 不区分大小写匹配 usercode
    const user = mockUsers.find(u =>
      u.usercode.toLowerCase() === params.usercode.toLowerCase() &&
      u.password === params.password
    )
    
    if (!user) {
      return Promise.resolve({
        code: 1,
        message: '用户编码或密码错误',
        data: null as unknown as LoginResponse
      })
    }
    
    // ... 其余逻辑保持不变
  }
  return request.post('/auth/login', params)
}
```

- [ ] **Step 3: 运行类型检查验证（在 Step 1-2 完成后）**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "api/auth.ts"`
Expected: 无错误输出（LoginParams 和 mock 登录逻辑已更新）

- [ ] **Step 4: 提交 API auth 变更**

```bash
git add frontend/src/api/auth.ts
git commit -m "feat(api): change login param from username to usercode"
```

---

### Task 4: 更新 Auth Store 登录方法

**Files:**
- Modify: `frontend/src/stores/auth.ts`

- [ ] **Step 1: 更新 login 方法参数**

将 `username` 参数改为 `usercode`：

```typescript
async login(usercode: string, password: string): Promise<boolean> {
  try {
    const response = await loginApi({ usercode, password })
    // ... 其余逻辑保持不变
  } catch (error) {
    ElMessage.error('用户编码或密码错误')  // 改：原为"账号或密码错误"
    return false
  }
}
```

- [ ] **Step 2: 更新错误提示文案**

将所有登录相关的错误提示中的"账号"改为"用户编码"：

```typescript
ElMessage.error('用户编码或密码错误')
```

- [ ] **Step 3: 运行类型检查验证**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "stores/auth.ts"`
Expected: 无错误输出

- [ ] **Step 4: 提交 Auth Store 变更**

```bash
git add frontend/src/stores/auth.ts
git commit -m "feat(store): change auth login param to usercode"
```

---

### Task 5: 添加 usercode 唯一性校验 API

**Files:**
- Modify: `frontend/src/api/user.ts`

- [ ] **Step 1: 导入 checkUsercodeExists 函数**

在文件顶部添加导入：

```typescript
import { mockUsers, checkUsercodeExists } from '@/mock/user'
```

- [ ] **Step 2: 导出 checkUsercodeExists 函数**

在文件末尾导出函数供组件使用：

```typescript
export { checkUsercodeExists }
```

- [ ] **Step 3: 运行类型检查验证**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "api/user.ts"`
Expected: 无错误输出

- [ ] **Step 4: 提交 API user 变更**

```bash
git add frontend/src/api/user.ts
git commit -m "feat(api): export checkUsercodeExists for user edit validation"
```

---

## Chunk 3: 界面更新

### Task 6: 更新登录页面

**Files:**
- Modify: `frontend/src/views/Login.vue`

- [ ] **Step 1: 更新 loginForm 字段名**

将 `username` 改为 `usercode`：

```typescript
const loginForm = reactive({
  usercode: '',   // 改：原为 username
  password: ''
})
```

- [ ] **Step 2: 更新输入框绑定和 placeholder**

修改用户编码输入框：

```vue
<el-input
  v-model="loginForm.usercode"
  placeholder="请输入用户编码"
  prefix-icon="User"
  size="large"
/>
```

- [ ] **Step 3: 更新登录调用**

确认 `handleLogin` 函数使用正确的字段名：

```typescript
const handleLogin = async () => {
  loading.value = true
  const success = await authStore.login(loginForm.usercode, loginForm.password)
  // ... 其余逻辑保持不变
}
```

- [ ] **Step 4: 运行类型检查验证**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "Login.vue"`
Expected: 无错误输出

- [ ] **Step 5: 提交登录页面变更**

```bash
git add frontend/src/views/Login.vue
git commit -m "feat(login): change username input to usercode"
```

---

### Task 7: 更新用户管理列表

**Files:**
- Modify: `frontend/src/views/UserManagement.vue`

- [ ] **Step 1: 在表格添加用户编码列**

在 el-table-columns 中添加新列，位于第一列：

```vue
<el-table-column prop="usercode" label="用户编码" width="120" />
<el-table-column prop="username" label="昵称" width="120" />
<!-- 其余列保持不变 -->
```

- [ ] **Step 2: 运行类型检查验证**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "UserManagement.vue"`
Expected: 无错误输出

- [ ] **Step 3: 提交用户管理列表变更**

```bash
git add frontend/src/views/UserManagement.vue
git commit -m "feat(user-mgmt): add usercode column to user table"
```

---

### Task 8: 更新用户编辑对话框

**Files:**
- Modify: `frontend/src/components/settings/UserEditDialog.vue`

- [ ] **Step 1: 导入 checkUsercodeExists 函数（依赖 Task 5 导出的函数）**

在 script setup 中添加导入：

```typescript
import { checkUsercodeExists } from '@/api/user'
```

- [ ] **Step 2: 在 formData 添加 usercode 字段**

在 reactive formData 中添加 usercode：

```typescript
const formData = reactive<UserFormData>({
  usercode: '',      // 新增
  username: '',
  password: '',
  roleId: 0,
  hisStaffId: null,
  enabled: true
})
```

- [ ] **Step 3: 在表单添加 usercode 输入框**

在 el-form 中添加 usercode 输入框，位于第一个字段：

```vue
<el-form-item label="用户编码" prop="usercode" :rules="usercodeRules">
  <el-input v-model="formData.usercode" placeholder="请输入用户编码" />
</el-form-item>
```

- [ ] **Step 4: 定义 usercode 校验规则**

添加 usercode 的校验规则：

```typescript
const usercodeRules = [
  { required: true, message: '请输入用户编码', trigger: 'blur' },
  {
    validator: (rule: any, value: string, callback: any) => {
      if (!value) {
        callback()
        return
      }
      // 编辑时排除当前用户
      const excludeId = props.modelValue ? props.user?.id : undefined
      if (checkUsercodeExists(value, excludeId)) {
        callback(new Error('用户编码已存在'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]
```

- [ ] **Step 5: 更新 initFormData 函数**

在打开对话框时初始化 usercode：

```typescript
const initFormData = () => {
  if (props.user) {
    formData.usercode = props.user.usercode    // 新增
    formData.username = props.user.username
    formData.password = ''
    formData.roleId = props.user.roleId
    formData.hisStaffId = props.user.hisStaffId
    formData.enabled = props.user.enabled
  } else {
    formData.usercode = ''                     // 新增
    formData.username = ''
    formData.password = ''
    formData.roleId = 0
    formData.hisStaffId = null
    formData.enabled = true
  }
}
```

- [ ] **Step 6: 运行类型检查验证**

Run: `cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "UserEditDialog.vue"`
Expected: 无错误输出

- [ ] **Step 7: 提交用户编辑对话框变更**

```bash
git add frontend/src/components/settings/UserEditDialog.vue
git commit -m "feat(user-edit): add usercode field with uniqueness validation"
```

---

## Chunk 4: 最终验证

### Task 9: 运行完整类型检查和构建

**Files:**
- 无文件变更，仅验证

- [ ] **Step 1: 运行完整类型检查**

Run: `cd frontend && npm run type-check`
Expected: 无错误，所有类型检查通过

- [ ] **Step 2: 运行 ESLint 检查**

Run: `cd frontend && npm run lint`
Expected: 无错误，或仅有非本项目引入的警告

- [ ] **Step 3: 运行构建**

Run: `cd frontend && npm run build`
Expected: 构建成功，无错误

- [ ] **Step 4: 启动开发服务器进行手动测试**

Run: `cd frontend && npm run dev`

手动测试：
1. 登录页面：输入用户编码（尝试不同大小写组合）
2. 用户管理：查看用户编码列显示
3. 用户编辑：创建新用户，测试 usercode 必填和唯一性校验
4. 用户编辑：编辑现有用户，确认 usercode 可修改且唯一性校验排除自身

---

### Task 10: 更新设计文档状态

**Files:**
- Modify: `docs/superpowers/specs/2026-04-09-user-usercode-design.md`

- [ ] **Step 1: 更新文档状态**

将状态从"待用户审核"改为"已实现"：

```markdown
**状态**: 已实现
```

- [ ] **Step 2: 提交设计文档更新**

```bash
git add docs/superpowers/specs/2026-04-09-user-usercode-design.md
git commit -m "docs: mark usercode design as implemented"
```

---

## 实现总结

**文件变更清单（共 8 个文件）:**
1. `frontend/src/types/user.ts` - 类型定义
2. `frontend/src/mock/user.ts` - Mock 数据和校验函数
3. `frontend/src/api/auth.ts` - 登录 API
4. `frontend/src/stores/auth.ts` - Auth Store
5. `frontend/src/api/user.ts` - 导出校验函数
6. `frontend/src/views/Login.vue` - 登录页面
7. `frontend/src/views/UserManagement.vue` - 用户管理列表
8. `frontend/src/components/settings/UserEditDialog.vue` - 用户编辑对话框

**关键实现点:**
- usercode 字段添加到 User 和 UserFormData 接口
- 登录使用 usercode，不区分大小写匹配
- Mock 数据添加 usercode 值
- checkUsercodeExists 函数实现不区分大小写的唯一性校验
- 界面更新：登录页、用户列表、用户编辑对话框