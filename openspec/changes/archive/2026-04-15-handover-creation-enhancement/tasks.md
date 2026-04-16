## 1. 数据库字段变更

**功能模块：在院患者标识**

- [x] 1.1 在 visit 表添加 fg_ip 字段（VARCHAR(1), 'Y'=在院, 'N'=不在院）
- [x] 1.2 创建 Flyway 迁移脚本添加 fg_ip 字段
- [x] 1.3 在 Visit 实体类添加 fgIp 字段和 getter/setter
- [x] 1.4 更新现有数据，设置 fg_ip 默认值（discharge_datetime IS NULL -> 'Y'）

## 2. 后端统计 API 实现

**功能模块：患者统计自动计算**

- [x] 2.1 创建 HandoverStatsDto 数据传输对象
- [x] 2.2 创建 HandoverController.getStats() 接口方法
- [x] 2.3 在 VisitRepository 添加 countTotalPatients() 方法（包含 fg_ip='Y' 条件）
- [x] 2.4 在 VisitRepository 添加 countAdmission24h() 方法（包含 fg_ip='Y' 条件）
- [x] 2.5 在 VisitRepository 添加 countDischarge24h() 方法（包含 fg_ip='N' 条件）
- [x] 2.6 在 TransferRecordRepository 添加 countTransferOut24h() 方法
- [x] 2.7 在 TransferRecordRepository 添加 countTransferIn24h() 方法
- [x] 2.8 在 HandoverService 实现 calculateStats() 方法
- [x] 2.9 测试统计 API 返回正确数据

## 2. 后端值班人员 API 实现

**功能模块：值班人员默认选择**

- [x] 2.1 创建 DutyStaffDto 数据传输对象
- [x] 2.2 创建 HandoverController.getDutyStaff() 接口方法
- [x] 2.3 在 SchedulingDetailRepository 添加 findTodayDutyStaff() 方法
- [x] 2.4 在 HandoverService 实现 getDutyStaff() 方法
- [x] 2.5 处理无排班数据和排班未发布的情况
- [x] 2.6 测试值班人员 API 返回正确数据

## 3. 后端患者筛选逻辑优化

**功能模块：交班患者筛选**

- [x] 3.1 优化 VisitRepository.findHandoverPatients() 查询条件
- [x] 3.2 添加在院条件（fg_ip='Y' AND (discharge_datetime IS NULL OR discharge_datetime > NOW()))
- [x] 3.3 确保24小时内入院条件正确（admission_datetime >= cutoff）
- [x] 3.4 确保 I级护理条件正确（nurse_level_code = '01')
- [x] 3.5 在 HandoverPatientDto 添加 filterReason 字段逻辑
- [x] 3.6 测试患者筛选返回正确列表（仅返回在院患者）

## 4. 后端临时医嘱填充逻辑

**功能模块：临时医嘱显示**

- [x] 4.1 验证 OrderMainRepository.findTemporaryOrdersWithin24h() 方法
- [x] 4.2 优化临时医嘱内容格式化逻辑（医嘱类别: 医嘱内容）
- [x] 4.3 处理医嘱内容过长截断（>100字符）
- [x] 4.4 在 HandoverService.buildCurrentCondition() 中完善逻辑
- [x] 4.5 测试临时医嘱正确填充到"目前情况"字段

## 5. 前端 API 层更新

**功能模块：发起交班前端集成**

- [x] 5.1 在 handover.ts 添加 fetchHandoverStats() 方法
- [x] 5.2 在 handover.ts 添加 fetchDutyStaff() 方法
- [x] 5.3 定义 HandoverStatsDto 类型接口
- [x] 5.4 定义 DutyStaffDto 类型接口

## 6. 前端 CreateHandoverPage 修改

**功能模块：发起交班前端集成**

- [x] 6.1 修改 form.shift 默认值从 '白班' 改为 '夜班'
- [x] 6.2 移除死亡、手术、病危统计项的显示
- [x] 6.3 将统计项改为只读显示（移除 el-input-number）
- [x] 6.4 添加 onMounted 时调用 fetchHandoverStats() 加载统计数据
- [x] 6.5 添加 onMounted 时调用 fetchDutyStaff() 设置接班医生默认值
- [x] 6.6 处理无值班人员时的提示信息显示
- [x] 6.7 移除 getHandoverPatients mock 数据调用，改为调用真实 API
- [x] 6.8 移除 allDoctors 硬编码数据，改为从真实 API 获取科室人员

## 7. 前端患者列表显示优化

**功能模块：交班患者筛选**

- [x] 7.1 在患者表格添加 filterReason 列显示筛选原因
- [x] 7.2 确认"目前情况"字段正确显示临时医嘱内容
- [x] 7.3 测试患者列表显示符合筛选条件的患者

## 8. 集成测试

**功能模块：发起交班功能验证**

- [ ] 8.1 测试班次默认值为夜班
- [ ] 8.2 测试有排班数据时接班医生自动选择
- [ ] 8.3 测试无排班数据时显示提示信息
- [ ] 8.4 测试患者统计数据正确显示
- [ ] 8.5 测试死亡/手术/病危统计项不显示
- [ ] 8.6 测试患者列表仅显示在院患者（fg_ip='Y')
- [ ] 8.7 测试患者列表仅显示24小时内入院和I级护理患者
- [ ] 8.8 测试筛选原因标签正确显示
- [ ] 8.9 测试临时医嘱正确填充到"目前情况"
- [ ] 8.10 测试用户可编辑修改"目前情况"内容
- [ ] 8.11 测试切换科室后数据正确更新