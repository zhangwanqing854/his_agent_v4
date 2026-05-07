## 上下文

当前系统已有用户管理和角色管理功能，但存在功能缺失：

**用户管理现状：**
- `User.java` 有 `isSuperAdmin` 字段（Boolean, 默认 false）
- `UserDto.java` 返回 `isSuperAdmin` 用于显示
- `UserUpdateRequest.java` 无 `isSuperAdmin` 字段 → 无法修改
- `UserService.updateUser()` 不处理 `isSuperAdmin`
- `UserEditDialog.vue` 显示超管标签，但无编辑控件

**角色职责现状：**
- `RoleDto.java` 返回 `dutyIds: List<Long>`
- 前端 `Role` 类型期望 `duties?: Duty[]`
- `RoleDutiesDialog.vue` 直接使用 `role.duties`，可能导致空显示

## 目标 / 非目标

**目标：**
- 超管标识可修改（安全规则约束）
- 角色职责查看时数据完整加载

**非目标：**
- 不修改认证/权限判断逻辑
- 不新增职责 CRUD
- 不修改数据库表结构

## 决策

### 决策 1：超管修改权限控制

**方案对比：**
| 方案 | 描述 | 优点 | 缺点 |
|------|------|------|------|
| A: 后端校验 | Service层检查操作者权限 | 安全、统一 | 需传操作者ID |
| B: 前端隐藏 | UI不显示控件 | 简单 | 可绕过 |
| C: 混合 | 前端隐藏+后端校验 | 双重保护 | 最佳 |

**选择：方案 C（混合）**
- 前端：只有超管登录时才显示"超管"开关
- 后端：校验操作者必须是超管，且不能修改自己

### 决策 2：职责数据加载方式

**方案对比：**
| 方案 | 描述 | 优点 | 缺点 |
|------|------|------|------|
| A: RoleDto扩展 | 返回 duties 数组 | 一请求获取 | 响应变大 |
| B: Dialog加载 | 打开时调用 /roles/{id}/duties | 按需加载 | 多请求 |
| C: 混合 | RoleDto含duties + Dialog兜底 | 完整 | 稍复杂 |

**选择：方案 A（RoleDto扩展）**
- 简化前端逻辑，一次请求获取完整数据
- RoleDto添加 `duties: List<DutyDto>` 字段

### 决策 3：最后一个超管保护

**方案：**
- 后端 `UserService.updateUser()` 检查：如果要移除超管，先统计当前超管数量
- 如果只剩一个超管，拒绝修改，抛出异常

## 风险 / 权衡

| 风险 | 缓解措施 |
|------|----------|
| 操作者身份伪造 | 后端从 JWT Token 提取用户ID，不信任请求参数 |
| 前端绕过校验 | 后端必须有校验，前端只是 UX 增强 |
| duties 加载性能 | RoleDto.duties 只含必要字段（id, name, code） |
| 超管数量统计慢 | 使用 count 查询，不加载全部用户 |