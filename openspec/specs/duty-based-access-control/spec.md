## ADDED Requirements

### 需求:用户登录时获取职责列表

系统在用户登录成功后，必须返回该用户所拥有的职责列表。

#### 场景:用户登录成功返回职责

- **当** 用户使用正确的用户名和密码登录
- **那么** 系统返回 UserInfo 包含 duties 字段，列出用户角色关联的所有职责（id、code、name）

#### 场景:用户角色无职责

- **当** 用户登录但其角色未分配任何职责
- **那么** 系统返回 UserInfo 的 duties 字段为空数组

#### 场景:超级管理员登录

- **当** isSuperAdmin=true 的用户登录
- **那么** 系统返回 UserInfo 的 duties 字段包含所有已定义的职责

### 需求:前端存储用户职责列表

前端 authStore 必须存储登录返回的用户职责列表，并提供查询方法。

#### 场景:authStore 存储职责

- **当** 用户登录成功
- **那么** authStore.userInfo.duties 包含用户职责列表

#### 场景:职责查询方法

- **当** 调用 authStore.hasDuty('PATIENT_MANAGEMENT')
- **那么** 如果用户职责列表包含 PATIENT_MANAGEMENT，返回 true；否则返回 false

#### 场景:超级管理员职责查询

- **当** isSuperAdmin=true 的用户调用 hasDuty(any_code)
- **那么** 始终返回 true

### 需求:Dashboard卡片职责过滤

Dashboard 页面必须根据用户职责过滤显示的功能卡片。

#### 场景:用户有职责显示卡片

- **当** 用户职责包含 PATIENT_MANAGEMENT
- **那么** Dashboard 显示"患者管理"功能卡片

#### 场景:用户无职责隐藏卡片

- **当** 用户职责不包含 USER_MANAGEMENT
- **那么** Dashboard 不显示"用户管理"功能卡片

#### 场景:超级管理员显示全部

- **当** isSuperAdmin=true 的用户访问 Dashboard
- **那么** 显示所有功能卡片

### 需求:路由访问职责检查

路由守卫必须检查用户是否有权限访问目标路由对应的职责。

#### 场景:有权限路由访问

- **当** 用户职责包含 DOCTOR_DEPARTMENT_MANAGEMENT
- **且** 用户访问 /doctor-department 路由
- **那么** 允许访问，页面正常加载

#### 场景:无权限路由访问

- **当** 用户职责不包含 USER_MANAGEMENT
- **且** 用户访问 /users 路由
- **那么** 重定向到 403 提示页面

#### 场景:超级管理员路由访问

- **当** isSuperAdmin=true 的用户访问任意路由
- **那么** 允许访问，跳过职责检查

#### 场景:无权限页面显示

- **当** 用户被路由守卫拦截
- **那么** 显示 403 Forbidden 页面，提供返回首页按钮

### 需求:新增科室人员管理职责节点

系统必须定义 DOCTOR_DEPARTMENT_MANAGEMENT 职责节点。

#### 场景:职责定义

- **当** 查询 duties 列表
- **那么** 包含 id=11, code='DOCTOR_DEPARTMENT_MANAGEMENT', name='科室人员管理' 的记录

#### 场景:职责与路由对应

- **当** 路由 /doctor-department 对应职责 DOCTOR_DEPARTMENT_MANAGEMENT
- **那么** 有该职责的用户可以访问，无该职责的用户被拦截