## 为什么

发起交班页面当前使用硬编码的模拟数据，班次默认值、接班医生选择、患者统计和患者筛选逻辑均不符合业务规范。需要基于真实 HIS 数据实现正确的业务逻辑，确保交班信息准确反映科室实际情况。

## 变更内容

1. **班次默认值调整**：将班次默认值从"白班"改为"夜班"
2. **接班医生默认值**：默认选择当天值班人员（从排班表获取），可修改，范围限定为当前科室人员
3. **患者人数统计自动计算**：
   - 患者总数：当前科室在院患者总数（fg_ip='Y')
   - 入院人数：24小时内入院患者数
   - 转出人数：24小时内转出科室的患者数
   - 出院人数：24小时内出院患者数
   - 转入人数：24小时内转入科室的患者数
   - 移除显示：死亡、手术、病危人数（业务要求不显示）
4. **数据库字段新增**：visit 表新增 fg_ip 字段（在院标识）
   - 'Y' = 在院
   - 'N' = 不在院
5. **交班患者列表筛选**：只显示符合条件的患者：
   - 在院患者（fg_ip='Y' 且 discharge_datetime > 当前时间）
   - 24小时内入院的患者
   - I级护理的患者
6. **目前情况自动填充**：显示患者24小时内的临时医嘱信息

## 功能 (Capabilities)

### 新增功能

- `handover-stats-calculation`: 患者人数统计自动计算功能，从 HIS 系统实时获取入院、转出、出院、转入数据
- `duty-staff-default`: 值班人员默认选择功能，从排班表获取当天值班医生作为接班医生默认值
- `temporary-order-display`: 临时医嘱显示功能，自动加载患者24小时内临时医嘱填充"目前情况"

### 修改功能

- `handover-creation`: 发起交班功能需求变更
  - 班次默认值变更：白班 → 夜班
  - 患者筛选逻辑变更：全量显示 → 仅显示在院患者（fg_ip='Y')且符合24小时内入院或I级护理条件
  - 统计数据来源变更：手动输入 → 自动计算
  - 接班医生默认值变更：无默认 → 当天值班人员

## 影响

### 前端影响
- `frontend/src/views/CreateHandoverPage.vue`: 班次默认值、统计数据展示、患者列表加载逻辑
- `frontend/src/api/handover.ts`: 新增统计 API 和值班人员 API 调用
- `frontend/src/mock/patient.ts`: 移除或调整为备用数据源

### 后端影响
- `backend/.../controller/HandoverController.java`: 新增统计接口
- `backend/.../service/HandoverService.java`: 统计计算逻辑、患者筛选逻辑优化
- `backend/.../repository/VisitRepository.java`: 新增统计查询方法
- `backend/.../repository/TransferRecordRepository.java`: 新增转科统计查询方法
- `backend/.../repository/OrderMainRepository.java`: 临时医嘱查询优化

### HIS 系统对接
- 患者统计数据：visit 表（入院时间、出院时间、护理等级、在院标识 fg_ip）
- 转科数据：transfer_record 表（转出科室、转入科室、转科时间）
- 医嘱数据：order_main 表（临时医嘱标识、医嘱时间）
- 排班数据：scheduling/scheduling_detail 表（当天值班人员）

### 数据库变更
- visit 表新增 fg_ip 字段（VARCHAR(1), 默认值根据 discharge_datetime 设置）

## 非目标

- 不修改交班报告打印格式
- 不修改语音录入功能
- 不修改交班确认流程
- 不实现死亡、手术、病危人数统计（业务明确要求不显示）