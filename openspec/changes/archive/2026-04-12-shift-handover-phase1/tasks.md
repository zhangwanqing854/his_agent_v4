# Phase 2 后端实现任务清单

**策略**：前端已使用 Mock 数据完成开发，现在实现后端 API。每个任务不超过 2 小时。

**当前状态**：
- ✅ 前端项目已完成（Vue 3 + TypeScript + Element Plus）
- ✅ 后端项目已创建（Spring Boot 3.2.5 + JDK 17）
- ✅ 数据库已安装（MySQL 9.6.0）
- ✅ 21张表已创建
- ✅ JPA 实体类已创建
- ✅ Repository 接口已创建

---

## 1. 基础设施搭建

- [x] 1.1.1 创建通用 DTO 类（ApiResponse、分页请求/响应）
- [x] 1.1.2 配置 JWT 依赖和工具类
- [x] 1.1.3 编写 JwtUtil 单元测试

## 2. 用户登录模块

- [x] 1.2.1 创建登录相关 DTO（LoginRequest、LoginResponse、UserInfo）
- [x] 1.2.2 实现 AuthService 登录逻辑（用户验证、Token 生成）
- [x] 1.2.3 实现 AuthController（/api/auth/login、/logout、/me）
- [x] 1.2.4 编写 AuthService 单元测试
- [x] 1.2.5 编写 AuthController 单元测试

## 3. 科室管理

- [x] 1.4.1 创建科室相关 DTO（DepartmentDto、DepartmentListResponse）
- [x] 1.4.2 实现 DepartmentService（列表、详情、按编码查询）
- [x] 1.4.3 实现 DepartmentController（/api/departments）
- [x] 1.4.4 编写 Department 单元测试

## 4. 用户管理模块

- [x] 2.1.1 创建用户相关 DTO（UserDto、UserCreateRequest、UserUpdateRequest）
- [x] 2.1.2 实现 UserService - 列表和详情查询
- [x] 2.1.3 实现 UserService - 创建和更新用户
- [x] 2.1.4 实现 UserService - 删除、启用禁用、重置密码
- [x] 2.1.5 实现 UserController（/api/users）
- [x] 2.1.6 编写 UserService 单元测试
- [x] 2.1.7 编写 UserController 单元测试

## 5. 角色管理模块

- [x] 2.2.1 创建角色相关 DTO（RoleDto、RoleCreateRequest、RoleUpdateRequest）
- [x] 2.2.2 实现 RoleService（CRUD + 职责分配）
- [x] 2.2.3 实现 RoleController（/api/roles）
- [x] 2.2.4 编写 Role 单元测试

## 6. 权限职责模块

- [x] 2.3.1 实现 PermissionService 和 Controller（/api/permissions）
- [x] 2.3.2 实现 DutyService 和 Controller（/api/duties）
- [x] 2.3.3 实现角色职责查询接口（/api/roles/{id}/duties）
- [x] 2.3.4 编写权限职责单元测试

## 7. HIS 医护人员管理

- [x] 2.4.1 实现 HisStaffService（列表、未关联用户查询）
- [x] 2.4.2 实现 HisStaffController（/api/his-staff）
- [x] 2.4.3 编写 HisStaff 单元测试

## 8. 接口配置模块

- [x] 3.1.1 创建接口配置 DTO（InterfaceConfigDto、MappingTableDto、FieldMappingDto）
- [x] 3.1.2 实现 InterfaceConfigService - 列表和详情
- [x] 3.1.3 实现 InterfaceConfigService - 创建和更新（含子表）
- [x] 3.1.4 实现 InterfaceConfigController（/api/interface-configs）
- [x] 3.1.5 编写 InterfaceConfig 单元测试

## 9. 患者管理模块

- [x] 4.1.1 创建患者相关 DTO（PatientDto、PatientListRequest、VisitDto）
- [x] 4.1.2 实现 PatientService（列表、详情、按科室筛选）
- [x] 4.1.3 实现 PatientController（/api/patients）
- [x] 4.1.4 编写 Patient 单元测试

## 10. 交班管理模块

- [x] 4.2.1 创建交班相关 DTO（HandoverDto、HandoverCreateRequest、HandoverPatientDto）
- [x] 4.2.2 实现 HandoverService - 列表和详情
- [x] 4.2.3 实现 HandoverService - 发起交班（筛选患者、创建记录）
- [x] 4.2.4 实现 HandoverController（/api/handovers）
- [x] 4.2.5 编写 Handover 单元测试

## 11. 统计分析模块

- [x] 4.3.1 创建统计相关 DTO（StatisticsDto）
- [x] 4.3.2 实现 StatisticsService（科室统计、交班统计）
- [x] 4.3.3 实现 StatisticsController（/api/statistics）
- [x] 4.3.4 编写 Statistics 单元测试

---

**✅ 全部任务已完成 (37/37)**

**每个任务产出**：
- Service 实现
- Controller 实现
- 单元测试通过