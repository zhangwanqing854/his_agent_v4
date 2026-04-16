## 1. 数据库设计与迁移

- [x] 1.1 创建 scheduling 表（排班主表）
- [x] 1.2 创建 scheduling_detail 表（排班明细表）
- [x] 1.3 添加外键约束（关联 department、his_staff）
- [x] 1.4 编写 Flyway 迁移脚本

## 2. 后端实体与 Mapper

- [x] 2.1 创建 Scheduling 实体类
- [x] 2.2 创建 SchedulingDetail 实体类
- [x] 2.3 创建 SchedulingRepository 接口
- [x] 2.4 创建 SchedulingDetailRepository 接口
- [x] 2.5 创建 DepartmentSchedulingConfigRepository 接口

## 3. 后端 DTO 与 Service

- [x] 3.1 创建 SchedulingDto（排班详情响应）
- [x] 3.2 创建 SchedulingDetailDto（排班明细响应）
- [x] 3.3 创建 CreateSchedulingRequest（创建排班请求）
- [x] 3.4 创建 UpdateSchedulingDetailsRequest（更新明细请求）
- [x] 3.5 创建 SchedulingConfigDto（排班配置响应）
- [x] 3.6 实现 SchedulingService（核心业务逻辑）
- [x] 3.7 实现科室数据隔离逻辑
- [x] 3.8 实现月份排班唯一性校验

## 4. 后端 Controller

- [x] 4.1 创建 SchedulingController
- [x] 4.2 实现 GET /api/scheduling（获取排班列表）
- [x] 4.3 实现 GET /api/scheduling/{id}（获取排班详情）
- [x] 4.4 实现 POST /api/scheduling（创建排班计划）
- [x] 4.5 实现 PUT /api/scheduling/{id}（更新排班状态）
- [x] 4.6 实现 DELETE /api/scheduling/{id}（删除排班计划）
- [x] 4.7 实现 PUT /api/scheduling/{id}/details（批量更新明细）
- [x] 4.8 实现 GET /api/scheduling/staff（获取可排班人员）
- [x] 4.9 实现 GET /api/scheduling/config（获取排班配置）
- [x] 4.10 实现 PUT /api/scheduling/config（更新排班配置）
- [x] 4.11 实现 POST /api/scheduling/{id}/auto-generate（快速生成）

## 5. 前端 API 层

- [x] 5.1 创建 api/scheduling.ts
- [x] 5.2 实现 fetchSchedulingList API
- [x] 5.3 实现 fetchSchedulingDetail API
- [x] 5.4 实现 createScheduling API
- [x] 5.5 实现 updateScheduling API
- [x] 5.6 实现 deleteScheduling API
- [x] 5.7 实现 updateSchedulingDetails API
- [x] 5.8 实现 fetchSchedulingStaff API

## 6. 前端路由配置

- [x] 6.1 添加 /scheduling 路由到 router/index.ts
- [x] 6.2 添加路由元信息（title: '科室排班', requiresAuth: true）

## 7. 前端排班管理页面

- [x] 7.1 创建 SchedulingManagement.vue 主页面
- [x] 7.2 实现月份选择器组件
- [x] 7.3 实现排班列表展示
- [x] 7.4 实现创建排班按钮和对话框
- [x] 7.5 实现排班状态切换（草稿/已发布）
- [x] 7.6 实现排班删除功能

## 8. 前端排班编辑对话框

- [x] 8.1 创建 SchedulingEditDialog.vue
- [x] 8.2 实现日期点击编辑功能
- [x] 8.3 实现科室人员下拉选择
- [x] 8.4 实现备注输入
- [x] 8.5 实现批量保存明细功能

## 9. 前端排班卡片组件

- [x] 9.1 创建 SchedulingCard.vue 卡片组件
- [x] 9.2 实现卡片布局样式（按周排列）
- [x] 9.3 实现日期、人员信息展示
- [x] 9.4 实现周末颜色区分（周六/周日灰色背景）
- [x] 9.5 实现空值班"待安排"提示

## 10. 前端打印功能

- [x] 10.1 创建 SchedulingPrint.vue 打印模板
- [x] 10.2 实现双月横向布局（A4 纸适配）
- [x] 10.3 实现 CSS @media print 样式
- [x] 10.4 实现 @page 规则设置
- [x] 10.5 实现打印预览功能
- [x] 10.6 实现打印按钮触发 window.print()

## 11. 侧边栏导航更新

- [x] 11.1 添加"科室排班"菜单项到侧边栏
- [x] 11.2 配置菜单图标和路由跳转

## 14. 快速生成功能

- [x] 14.1 创建 SchedulingQuickGenerateDialog.vue 对话框组件
- [x] 14.2 实现人员排序上下调整功能
- [x] 14.3 实现日期范围选择功能
- [x] 14.4 实现首次排班起点选择功能
- [x] 14.5 实现非首次排班自动接续提示
- [x] 14.6 实现连续轮换算法逻辑
- [x] 14.7 实现生成按钮触发自动填充
- [x] 14.8 更新 SchedulingManagement.vue 集成快速生成
- [x] 14.9 更新 api/scheduling.ts 添加配置和生成 API

## 12. 单元测试

- [ ] 12.1 编写 SchedulingService 单元测试（后端）
- [ ] 12.2 编写 SchedulingController 单元测试（后端）
- [ ] 12.3 编写前端 API 调用测试
- [ ] 12.4 编写排班卡片组件测试

## 13. 验证与集成测试

- [ ] 13.1 验证排班创建流程
- [ ] 13.2 验证排班编辑流程
- [ ] 13.3 验证排班删除流程
- [ ] 13.4 验证打印输出样式（Chrome/Edge/Firefox）
- [ ] 13.5 验证科室数据隔离
- [ ] 13.6 验证人员选择范围限制

---

**备注：**
- 任务属于【科室排班管理】模块
- 数据库变更：新增 3 张表（scheduling、scheduling_detail、department_scheduling_config）
- 每天只有一个值班人员（不分班次）
- 快速生成支持跨月连续轮换
- 每个任务预计耗时 ≤ 2 小时
- 打印测试需在多浏览器验证