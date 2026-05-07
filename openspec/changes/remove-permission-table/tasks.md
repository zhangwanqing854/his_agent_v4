## 1. 后端 - 删除 Permission 相关文件

- [x] 1.1 删除 entity/Permission.java
- [x] 1.2 删除 dto/PermissionDto.java
- [x] 1.3 删除 repository/PermissionRepository.java
- [x] 1.4 删除 service/PermissionService.java
- [x] 1.5 删除 controller/PermissionController.java
- [x] 1.6 删除 test/PermissionServiceTest.java

## 2. 后端 - 修改 Duty 相关文件

- [x] 2.1 Duty.java 移除 permissionId 字段
- [x] 2.2 DutyDto.java 移除 permissionId 和 permissionName 字段
- [x] 2.3 DutyService.java 移除 permissionId 相关逻辑（如有）

## 3. 后端 - 修改 RoleService

- [x] 3.1 RoleService.java 移除 Permission 导入语句
- [x] 3.2 RoleService.java 移除 PermissionRepository 相关代码

## 4. 前端 - 类型定义

- [x] 4.1 types/user.ts 删除 Permission interface
- [x] 4.2 types/user.ts Duty 移除 permissionId 和 permission 字段

## 5. 前端 - API 层

- [x] 5.1 api/user.ts 删除 fetchPermissionList()
- [x] 5.2 api/user.ts 删除 fetchPermissionById()
- [x] 5.3 api/user.ts 删除 fetchDutiesByPermission()

## 6. 前端 - Mock 数据

- [x] 6.1 mock/user.ts 删除 mockPermissions

## 7. 前端 - RoleManagement.vue

- [x] 7.1 删除 permissionList ref
- [x] 7.2 删除 fetchPermissionList() 调用
- [x] 7.3 删除"权限管理" Tab
- [x] 7.4 RoleEditDialog 传参移除 permissions prop

## 8. 前端 - RoleEditDialog.vue

- [x] 8.1 移除 permissions prop 定义
- [x] 8.2 删除按权限分组逻辑（getDutiesByPermission）
- [x] 8.3 改为平铺显示职责 checkbox 列表
- [x] 8.4 删除权限全选逻辑

## 9. 前端 - RoleDutiesDialog.vue

- [x] 9.1 删除 permissions 硬编码列表
- [x] 9.2 删除按权限分组显示逻辑
- [x] 9.3 改为平铺显示职责列表

## 10. 数据库变更

- [x] 10.1 执行 ALTER TABLE duty DROP COLUMN permission_id
- [x] 10.2 执行 DROP TABLE permission（如有）
- [x] 10.3 更新 schema_v2.sql 移除 permission 表定义

## 11. 测试验证

- [x] 11.1 后端编译验证
- [x] 11.2 前端类型检查（无Permission相关错误）
- [x] 11.3 测试角色编辑职责分配功能（RoleEditDialog平铺职责checkbox）
- [x] 11.4 测试角色职责查看功能（RoleDutiesDialog平铺职责列表）
- [x] 11.5 测试 API 端点（/api/roles, /api/duties返回正确数据）