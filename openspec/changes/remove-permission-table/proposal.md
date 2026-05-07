## 为什么

Permission（权限）表在系统中作为职责（Duty）的分类层，但实际业务中角色直接关联职责即可满足需求，Permission 层成为冗余。当前 Permission 表无数据，导致职责分配界面无法正常显示，影响角色管理功能。

## 变更内容

**BREAKING** 移除 Permission 表及相关代码：

### 移除
- 后端：Permission entity、DTO、Repository、Service、Controller、Test
- 前端：Permission 类型定义、API、Mock 数据
- 数据库：permission 表、duty.permission_id 字段
- UI：RoleManagement.vue "权限管理" Tab、RoleEditDialog/RoleDutiesDialog 按权限分组逻辑

### 修改
- Duty entity/DTO：移除 permissionId、permissionName 字段
- RoleEditDialog.vue：职责分配改为平铺列表显示
- RoleDutiesDialog.vue：职责查看改为平铺列表显示

## 功能 (Capabilities)

### 新增功能
无新增功能

### 修改功能
- `role-duty-assignment`: 职责分配界面从按权限分组改为平铺列表

## 非目标

- 不修改角色-职责关联逻辑（RoleDuty 表保留）
- 不新增职责分类功能
- 不影响用户权限判断逻辑

## 影响

| 层级 | 影响范围 |
|------|----------|
| 后端 | 8 个文件（6 删除 + 2 修改） |
| 前端 | 8 个文件（类型、API、Mock、3 个 Vue 组件） |
| 数据库 | permission 表删除、duty 表字段修改 |
| API | `/api/permissions` 端点删除 |
| UI | 角色管理界面简化 |