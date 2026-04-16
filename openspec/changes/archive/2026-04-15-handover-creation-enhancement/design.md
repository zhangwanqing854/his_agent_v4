## 上下文

发起交班页面是医护交接班系统的核心功能入口。当前实现使用前端硬编码的模拟数据，包括：
- 班次默认值为"白班"
- 接班医生需要手动选择，无默认值
- 患者统计数据需要手动输入
- 患者列表显示所有在院患者，无筛选
- "目前情况"字段为空，需要手动填写

业务要求基于真实 HIS 数据实现正确的业务逻辑，涉及 visit（就诊）、transfer_record（转科）、order_main（医嘱）、scheduling_detail（排班）等多表关联查询。

## 目标 / 非目标

**目标：**
- 实现班次默认值为"夜班"
- 实现接班医生自动选择当天值班人员
- 实现患者统计自动计算（入院/转出/出院/转入）
- 实现患者列表筛选（24小时内入院 + I级护理）
- 实现临时医嘱自动填充"目前情况"

**非目标：**
- 不实现死亡、手术、病危人数统计
- 不修改交班报告打印格式
- 不修改语音录入功能

## 决策

### 1. 患者统计数据来源

**决策：** 后端统一计算统计数据，前端仅展示

**理由：**
- 统计逻辑涉及多表关联（visit、transfer_record），前端无法直接查询
- 后端计算可复用现有 Repository 方法，减少重复代码
- 单一 API 返回所有统计数据，减少前端请求次数

**替代方案：**
- 前端调用多个 API 分别获取数据 → 拒绝，请求次数多，用户体验差

### 2. 接班医生默认值获取方式

**决策：** 从排班表（scheduling_detail）获取当天值班人员

**理由：**
- 排班功能已实现，数据可靠
- 直接关联科室和日期，查询简单
- 与现有"科室排班"功能一致

**替代方案：**
- 从 doctor_department 表获取所有科室人员 → 按第一个排序 → 拒绝，不准确
- 新建值班人员表单独管理 → 拒绝，已有排班功能可复用

### 3. 患者筛选逻辑位置

**决策：** 后端筛选，前端直接展示结果

**理由：**
- 筛选条件涉及 admission_datetime 和 nurse_level_code，需要数据库查询
- VisitRepository 已有 `findHandoverPatients` 方法，可直接使用
- 后端筛选减少前端数据处理负担

**替代方案：**
- 前端获取全量数据后筛选 → 按业务规则过滤 → 拒绝，数据量大时性能差

### 4. 临时医嘱展示方式

**决策：** 在患者列表加载时一并获取临时医嘱，预填充"目前情况"字段

**理由：**
- OrderMainRepository 已有 `findTemporaryOrdersWithin24h` 方法
- 用户可编辑修改，预填充提高效率
- 与现有 HandoverService.createHandover 逻辑一致

### 5. API 设计

**决策：** 新增两个 API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/handovers/stats` | GET | 获取患者统计数据 |
| `/api/handovers/duty-staff` | GET | 获取当天值班人员 |

**理由：**
- 统计数据和值班人员是独立数据，分别获取更清晰
- 符合 RESTful 设计原则
- 前端可按需调用

## 风险 / 权衡

### 风险 1：排班数据可能不存在

**风险：** 当天排班计划未创建或未发布时，无法获取值班人员

**缓解措施：**
- 提供手动选择功能，用户可从科室人员列表中选择
- 前端显示提示："无排班数据，请手动选择接班医生"

### 风险 2：24小时时间窗口计算

**风险：** 服务器时间与 HIS 数据库时间可能不一致

**缓解措施：**
- 使用数据库时间（NOW()）计算时间窗口
- 查询时使用 `LocalDateTime.now().minusHours(24)` 作为 cutoff

### 风险 3：临时医嘱数据量大

**风险：** 部分患者24小时内可能有大量临时医嘱

**缓解措施：**
- 只显示医嘱类别和关键内容，不做完整展示
- 用户可编辑精简内容

## 数据模型

### 统计数据 DTO

```java
public class HandoverStatsDto {
    private Integer totalPatients;     // 在院患者总数
    private Integer admission;         // 24小时内入院
    private Integer transferOut;       // 24小时内转出
    private Integer discharge;         // 24小时内出院
    private Integer transferIn;        // 24小时内转入
}
```

### 值班人员 DTO

```java
public class DutyStaffDto {
    private Long staffId;
    private String staffName;
}
```

## 数据库查询逻辑

### 患者总数
```sql
SELECT COUNT(*) FROM visit 
WHERE department_id = ? 
AND fg_ip = 'Y' 
AND (discharge_datetime IS NULL OR discharge_datetime > NOW())
```

### 24小时内入院
```sql
SELECT COUNT(*) FROM visit 
WHERE department_id = ? 
AND admission_datetime >= (NOW() - INTERVAL 24 HOUR)
AND fg_ip = 'Y'
AND (discharge_datetime IS NULL OR discharge_datetime > NOW())
```

### 24小时内转出
```sql
SELECT COUNT(*) FROM transfer_record 
WHERE from_dept_id = ? 
AND transfer_time >= (NOW() - INTERVAL 24 HOUR)
```

### 24小时内出院
```sql
SELECT COUNT(*) FROM visit 
WHERE dept_id = ? 
AND discharge_datetime >= (NOW() - INTERVAL 24 HOUR)
AND fg_ip = 'N'
```

### 24小时内转入
```sql
SELECT COUNT(*) FROM transfer_record 
WHERE to_dept_id = ? 
AND transfer_time >= (NOW() - INTERVAL 24 HOUR)
```

### 当天值班人员
```sql
SELECT sd.staff_id, hs.name 
FROM scheduling_detail sd
JOIN scheduling s ON sd.scheduling_id = s.id
JOIN his_staff hs ON sd.staff_id = hs.id
WHERE s.department_id = ?
AND s.year_month = FORMAT(NOW(), 'yyyy-MM')
AND sd.duty_date = FORMAT(NOW(), 'yyyy-MM-dd')
AND s.status = 'published'
```

### 交班患者筛选
```sql
SELECT * FROM visit 
WHERE department_id = ?
AND fg_ip = 'Y'
AND (discharge_datetime IS NULL OR discharge_datetime > NOW())
AND (admission_datetime >= (NOW() - INTERVAL 24 HOUR) OR nurse_level_code = '01')
```