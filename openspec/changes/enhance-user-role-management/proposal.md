## 为什么

系统已有超级管理员标识（`isSuperAdmin`）和角色职责分配功能，但存在两个问题：
1. 超管标识只能显示，无法在界面上修改，导致无法灵活管理管理员权限
2. 角色职责分配时，后端返回 `dutyIds` 而前端期望 `duties` 数组，导致查看职责对话框数据可能为空

## 变更内容

### 用户管理增强
- 新增：用户编辑界面添加"超级管理员"开关控件
- 修改：后端 API 支持修改 `isSuperAdmin` 字段
- 规则：只有超管可修改其他用户的超管状态；超管不能修改自己；不能移除最后一个超管

### 角色职责分配增强
- 修改：`RoleDto` 返回 `duties` 数组（完整 DutyDto 信息）
- 修改：`RoleDutiesDialog` 打开时主动加载职责数据

## 功能 (Capabilities)

### 新增功能
- `super-admin-management`: 超级管理员标识的显示与修改功能

### 修改功能
- `role-duty-view`: 角色职责查看对话框的数据加载逻辑

## 非目标

- 不修改超级管理员的判断逻辑（认证/权限控制）
- 不新增职责管理 CRUD 功能（职责在角色编辑时已有分配）
- 不修改角色-职责关联表结构

## 影响

- 后端：`UserUpdateRequest.java`, `UserService.java`, `RoleDto.java`, `RoleService.java`
- 前端：`UserEditDialog.vue`, `RoleDutiesDialog.vue`, `user.ts` API
- API：PUT `/api/users/{id}` 增加 `isSuperAdmin` 参数