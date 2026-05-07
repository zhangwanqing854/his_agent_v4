## MODIFIED Requirements

### 需求:职责分配界面显示
系统必须在角色编辑界面显示所有可用职责，以平铺列表形式供用户选择。

#### 场景:显示职责列表
- **当** 用户打开角色编辑对话框
- **那么** 系统以平铺 checkbox 列表显示所有职责（交班管理、患者管理等）

#### 场景:选择职责
- **当** 用户勾选某个职责 checkbox
- **那么** 该职责 ID 加入 form.dutyIds 数组

#### 场景:取消选择职责
- **当** 用户取消勾选某个职责 checkbox
- **那么** 该职责 ID 从 form.dutyIds 数组移除

#### 场景:保存角色职责
- **当** 用户点击保存按钮
- **那么** 系统将 form.dutyIds 发送到后端 API 进行保存

## REMOVED Requirements

### 需求:按权限分组显示职责
**Reason**: Permission 表已删除，无分组依据
**Migration**: 职责直接平铺显示，不再按 Permission 分组

### 需求:权限管理 Tab
**Reason**: Permission 功能已移除
**Migration**: RoleManagement.vue 移除"权限管理" Tab，仅保留"角色管理"和"职责管理" Tab