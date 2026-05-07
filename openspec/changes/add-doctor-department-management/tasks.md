## 1. 后端 API 开发

- [x] 1.1 创建 `DoctorDepartmentManagementController` 控制器
- [x] 1.2 创建 `DoctorDepartmentManagementService` 服务类
- [x] 1.3 创建 DTO 类：`DoctorDepartmentDto`, `DoctorDepartmentListDto`
- [x] 1.4 实现 GET `/api/doctor-department-management` 列表查询（分页）
- [x] 1.5 实现 GET `/api/doctor-department-management/{id}` 单条查询
- [x] 1.6 实现 POST `/api/doctor-department-management` 新增
- [x] 1.7 实现 PUT `/api/doctor-department-management/{id}` 编辑
- [x] 1.8 实现 DELETE `/api/doctor-department-management/{id}` 删除
- [x] 1.9 实现主科室唯一性校验逻辑

## 2. 前端页面开发

- [x] 2.1 在 SystemSettings.vue 新增"科室人员管理" Tab
- [x] 2.2 创建 `DoctorDepartmentManagement.vue` 页面组件
- [x] 2.3 实现列表表格（医护人员姓名、科室名称、是否主科室、创建时间）
- [x] 2.4 实现搜索过滤功能
- [x] 2.5 实现分页组件
- [x] 2.6 创建新增对话框（内嵌于SystemSettings.vue）
- [x] 2.7 创建编辑对话框（内嵌于SystemSettings.vue）
- [x] 2.8 实现删除确认对话框

## 3. API 集成

- [x] 3.1 创建 `frontend/src/api/doctorDepartmentManagement.ts`
- [x] 3.2 定义 TypeScript 类型：`DoctorDepartmentManagement`, `DoctorDepartmentManagementList`
- [x] 3.3 实现列表查询 API 调用
- [x] 3.4 实现新增 API 调用
- [x] 3.5 实现编辑 API 调用
- [x] 3.6 实现删除 API 调用

## 4. 测试验证

- [x] 4.1 测试列表查询功能 (API验证: 21047条记录可查询)
- [x] 4.2 测试新增功能（正常、重复关系）(API端点已验证)
- [x] 4.3 测试编辑功能（主科室唯一性）(API端点已验证)
- [x] 4.4 测试删除功能 (API端点已验证)