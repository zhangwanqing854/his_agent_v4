## 1. 后端 - 超管管理

- [x] 1.1 在 UserUpdateRequest.java 添加 isSuperAdmin 字段
- [x] 1.2 在 UserService.updateUser() 处理 isSuperAdmin 修改
- [x] 1.3 实现超管修改权限校验（操作者必须是超管）
- [x] 1.4 实现不能修改自己的校验
- [x] 1.5 实现最后一个超管保护逻辑（count查询）
- [x] 1.6 UserController 从 JWT 提取操作者ID

## 2. 后端 - 角色职责

- [x] 2.1 在 RoleDto.java 添加 duties 字段（List<DutyDto>）
- [x] 2.2 修改 RoleService.toDto() 方法，填充 duties 数组
- [x] 2.3 确保 DutyDto 包含必要字段（id, code, name）

## 3. 前端 - 超管管理

- [x] 3.1 UserEditDialog.vue 添加"超级管理员"开关控件
- [x] 3.2 实现开关可见性控制（只有超管可见）
- [x] 3.3 实现自己编辑时开关禁用逻辑
- [x] 3.4 更新 updateUserApi 调用，传递 isSuperAdmin 参数
- [x] 3.5 处理最后一个超管错误提示

## 4. 前端 - 角色职责

- [x] 4.1 确保 RoleDutiesDialog.vue 正确使用 role.duties 数据
- [x] 4.2 测试职责显示按权限分组逻辑

## 5. 测试验证

- [x] 5.1 测试超管修改其他用户超管状态
- [x] 5.2 测试非超管不可修改超管状态
- [x] 5.3 测试超管不能修改自己
- [x] 5.4 测试最后一个超管保护
- [x] 5.5 测试角色职责查看对话框数据正确显示