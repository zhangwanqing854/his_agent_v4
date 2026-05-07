## 为什么

系统已有科室人员关系导入功能，但缺少日常维护功能。医院人员科室调动频繁，需要提供一个界面让管理员可以手动添加、编辑、删除科室人员关系，而不仅仅依赖批量导入。

## 变更内容

新增科室人员关系维护功能：
- 后端新增 CRUD API：查询、新增、编辑、删除科室人员关系
- 前端新增维护页面：列表展示、新增对话框、编辑对话框、删除确认
- 支持设置主科室（is_primary）标记

## 功能 (Capabilities)

### 新增功能
- `doctor-department-management`: 用户可在系统设置中维护科室人员关系，包括查看列表、新增、编辑、删除

### 修改功能
无（独立新增功能）

## 影响

- **后端**: 新增 DoctorDepartmentManagementController.java
- **前端**: 新增 DoctorDepartmentManagement.vue 页面组件
- **API**: 新增 GET/POST/PUT/DELETE `/api/doctor-department-management`
- **数据库**: 使用现有 doctor_department 表，无新增表结构

## 非目标

- 不修改科室人员关系导入功能
- 不实现批量操作功能
- 不修改 User 或 Department 表结构