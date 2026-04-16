## 1. 数据库设计

**功能模块：值班人员配置**

- [x] 1.1 创建 `department_duty_staff` 表
- [x] 1.2 创建对应索引和唯一约束

## 2. 后端实体和 Repository

**功能模块：值班人员配置**

- [x] 2.1 创建 `DepartmentDutyStaff` 实体类
- [x] 2.2 创建 `DepartmentDutyStaffRepository` 接口
- [x] 2.3 创建 `DepartmentDutyStaffDto` 数据传输对象

## 3. 后端 Service 实现

**功能模块：值班人员配置**

- [x] 3.1 创建 `DutyStaffService` 服务类
- [x] 3.2 实现获取科室值班人员列表方法
- [x] 3.3 实现添加值班人员方法（支持批量）
- [x] 3.4 实现移除值班人员方法
- [x] 3.5 实现调整人员排序方法
- [x] 3.6 实现一键初始化值班人员方法

## 4. 后端 Controller 实现

**功能模块：值班人员配置**

- [x] 4.1 创建 `DutyStaffController` 控制器
- [x] 4.2 实现 GET `/api/scheduling/duty-staff` 接口
- [x] 4.3 实现 POST `/api/scheduling/duty-staff` 接口
- [x] 4.4 实现 DELETE `/api/scheduling/duty-staff/{staffId}` 接口
- [x] 4.5 实现 PUT `/api/scheduling/duty-staff/order` 接口

## 5. 后端数据源切换

**功能模块：排班管理**

- [x] 5.1 修改 `SchedulingService.getSchedulableStaff()` 数据源
- [x] 5.2 实现值班人员表为空时的 fallback 逻辑
- [x] 5.3 修改 `SchedulingService.getConfig()` 初始化逻辑

## 6. 前端 API 层

**功能模块：值班人员配置**

- [x] 6.1 在 `scheduling.ts` 中添加值班人员配置 API 方法
- [x] 6.2 定义值班人员相关类型接口

## 7. 前端组件实现

**功能模块：值班人员配置**

- [x] 7.1 创建 `DutyStaffConfigDialog.vue` 组件
- [x] 7.2 实现值班人员列表展示
- [x] 7.3 实现添加值班人员功能（从工作人员列表选择）
- [x] 7.4 实现移除值班人员功能
- [x] 7.5 实现人员拖拽排序功能
- [x] 7.6 实现一键初始化功能

## 8. 前端页面集成

**功能模块：排班管理**

- [x] 8.1 在 `SchedulingManagement.vue` 添加"配置值班人员"按钮入口
- [x] 8.2 集成值班人员配置对话框
- [x] 8.3 修改 `SchedulingQuickGenerateDialog.vue` 人员列表来源提示

## 9. 测试验证

**功能模块：值班人员配置**

- [x] 9.1 测试值班人员添加功能
- [x] 9.2 测试值班人员移除功能
- [x] 9.3 测试人员排序调整功能
- [x] 9.4 测试一键初始化功能

**功能模块：排班管理**

- [x] 9.5 测试排班人员列表来源切换
- [x] 9.6 测试值班人员表为空时的 fallback 逻辑
- [x] 9.7 测试快速生成排班使用值班人员排序