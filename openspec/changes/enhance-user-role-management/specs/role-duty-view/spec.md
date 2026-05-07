## ADDED Requirements

### 需求:角色职责数据完整返回
系统必须在角色API返回中包含完整的职责信息数组。

#### 场景:RoleDto包含duties数组
- **当** 请求 GET /api/roles
- **那么** 返回的 RoleDto 包含 duties 字段，为数组类型，包含 id, code, name 字段

#### 场景:单角色查询包含duties
- **当** 请求 GET /api/roles/{id}
- **那么** 返回的 RoleDto 包含完整的 duties 数组

## MODIFIED Requirements

### 需求:职责查看对话框数据加载
RoleDutiesDialog组件必须能正确显示角色的职责信息。

#### 场景:打开对话框显示职责
- **当** 用户点击"查看职责"按钮
- **那么** 对话框按权限分组显示职责列表，每个职责显示名称和描述

#### 场景:无职责显示空状态
- **当** 角色无任何职责
- **那么** 对话框显示"暂无职责"空状态提示