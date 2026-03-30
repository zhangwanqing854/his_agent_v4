# 在院患者管理界面设计文档

**日期**: 2026-03-30
**状态**: 设计确认

---

## 1. 背景

### 1.1 现状分析

当前 `PatientManagement.vue` 已具备基本功能：
- 患者列表表格（床号、姓名、年龄、性别、诊断、风险评分）
- 简单搜索过滤（床号、姓名）
- 操作按钮（查看、编辑、交班）
- 分页功能

### 1.2 需求来源

- 在院患者数据来自 HIS 系统同步，**无新增/编辑功能**
- 需完善查看功能，提升医护人员对患者信息的获取效率

---

## 2. 功能需求

### 2.1 核心功能

| 功能 | 描述 |
|------|------|
| 患者详情查看 | 右侧滑出抽屉面板，展示完整患者信息 |
| 表格/卡片视图切换 | Tab切换两种视图模式 |
| 丰富表格列 | 增加入院日期、过敏史、护理等级、主治医生等列 |
| 扩展搜索过滤 | 增加护理等级、风险等级、入院日期范围筛选 |
| 详细卡片视图 | 卡片展示完整信息（含最新生命体征） |
| 生命体征趋势图 | ECharts图表展示最近7天数据变化 |

### 2.2 数据约束

- 所有数据来自 HIS 系统同步，前端只读
- 使用 Mock 数据模拟，后端逻辑暂不实现

---

## 3. 数据结构设计

### 3.1 患者完整信息接口

```typescript
// types/patient.ts

// 患者完整信息接口
export interface Patient {
  id: number
  bedNumber: string
  name: string
  age: number
  gender: '男' | '女'
  admissionDate: string           // 入院日期 (YYYY-MM-DD)
  hospitalId: string              // 住院号
  department: string              // 科室名称
  departmentId: number            // 科室ID
  diagnosis: string               // 诊断
  attendingDoctor: string         // 主治医生
  nurseLevel: '特级' | '一级' | '二级' | '三级'  // 护理等级
  isCritical: boolean             // 是否重点患者
  
  // 过敏史/病史
  allergies: string[]             // 过敏药物列表
  medicalHistory: string[]        // 既往病史
  
  // 风险评估
  mewsScore: number               // MEWS评分 (0-5)
  bradenScore: number             // Braden评分 (15-23)
  fallRisk: number                // 跌倒风险评分 (0-50)
  
  // 生命体征（历史记录）
  vitalsHistory: VitalRecord[]    // 生命体征历史记录
  
  // 护理信息
  nursingNotes: string            // 特殊护理事项
  
  // 关联交班记录
  handoverRecords: HandoverRecord[]  // 相关交班记录列表
}

// 生命体征记录
export interface VitalRecord {
  time: string                    // 测量时间 (YYYY-MM-DD HH:mm)
  temperature: number             // 体温 (°C)
  pulse: number                   // 脉搏 (次/分)
  respiration: number             // 呼吸 (次/分)
  systolicBp: number              // 收缩压 (mmHg)
  diastolicBp: number             // 舒张压 (mmHg)
  oxygenSaturation: number        // 血氧饱和度 (%)
}

// 交班记录（简化）
export interface HandoverRecord {
  id: number
  time: string                    // 交班时间
  fromDoctor: string              // 交班医生
  toDoctor: string                // 接班医生
  summary: string                 // 交班摘要
}

// 简化患者信息（用于表格/卡片列表）
export interface PatientListItem {
  id: number
  bedNumber: string
  name: string
  age: number
  gender: '男' | '女'
  admissionDate: string
  diagnosis: string
  attendingDoctor: string
  nurseLevel: '特级' | '一级' | '二级' | '三级'
  isCritical: boolean
  allergies: string[]
  mewsScore: number
  bradenScore: number
  fallRisk: number
  // 最新生命体征
  latestVitals: VitalRecord
}

// 筛选项类型
export interface PatientFilter {
  bedNumber: string
  name: string
  nurseLevel: '' | '特级' | '一级' | '二级' | '三级'
  riskLevel: '' | '高风险' | '中风险' | '低风险'
  admissionDateStart: string
  admissionDateEnd: string
}
```

### 3.2 Mock 数据生成器

扩展 `mock/patient.ts`，使用生成器模式：

```typescript
// mock/patient.ts

import type { Patient, VitalRecord, PatientListItem, HandoverRecord } from '@/types/patient'

// ==================== 工具函数 ====================

// 生成生命体征历史记录（最近7天）
function generateVitalsHistory(baseTemp: number = 36.5): VitalRecord[] {
  const records: VitalRecord[] = []
  const now = new Date()
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().slice(0, 10)
    
    // 每天4次测量（08:00, 12:00, 16:00, 20:00）
    for (const hour of [8, 12, 16, 20]) {
      records.push({
        time: `${dateStr} ${String(hour).padStart(2, '0')}:00`,
        temperature: baseTemp + Math.random() * 1.5 - 0.5,
        pulse: 60 + Math.floor(Math.random() * 40),
        respiration: 16 + Math.floor(Math.random() * 8),
        systolicBp: 110 + Math.floor(Math.random() * 40),
        diastolicBp: 70 + Math.floor(Math.random() * 20),
        oxygenSaturation: 95 + Math.floor(Math.random() * 5)
      })
    }
  }
  
  return records
}

// 生成交班记录
function generateHandoverRecords(patientId: number): HandoverRecord[] {
  const doctors = ['张医生', '李医生', '王医生', '赵医生']
  const records: HandoverRecord[] = []
  
  for (let i = 0; i < 3; i++) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const fromIdx = i % doctors.length
    const toIdx = (i + 1) % doctors.length
    
    records.push({
      id: patientId * 100 + i,
      time: `${date.toISOString().slice(0, 10)} 08:00`,
      fromDoctor: doctors[fromIdx],
      toDoctor: doctors[toIdx],
      summary: '患者情况稳定，继续观察'
    })
  }
  
  return records
}

// ==================== 各科室患者数据 ====================

const departmentPatients: Record<number, Patient[]> = {
  // 心内科 (科室ID: 1)
  1: [
    {
      id: 1,
      bedNumber: '1床',
      name: '张三',
      age: 65,
      gender: '男',
      admissionDate: '2026-03-10',
      hospitalId: 'H202603001',
      department: '心内科',
      departmentId: 1,
      diagnosis: '2型糖尿病、高血压3级',
      attendingDoctor: '王主任',
      nurseLevel: '一级',
      isCritical: true,
      allergies: ['青霉素'],
      medicalHistory: ['高血压病史10年', '糖尿病病史5年'],
      mewsScore: 2,
      bradenScore: 16,
      fallRisk: 25,
      vitalsHistory: generateVitalsHistory(37.0),
      nursingNotes: '每日监测血糖4次，注意低血糖症状观察',
      handoverRecords: generateHandoverRecords(1)
    },
    {
      id: 2,
      bedNumber: '2床',
      name: '李四',
      age: 58,
      gender: '男',
      admissionDate: '2026-03-12',
      hospitalId: 'H202603002',
      department: '心内科',
      departmentId: 1,
      diagnosis: '冠心病、心功能不全',
      attendingDoctor: '李主任',
      nurseLevel: '一级',
      isCritical: true,
      allergies: [],
      medicalHistory: ['冠心病病史3年'],
      mewsScore: 1,
      bradenScore: 18,
      fallRisk: 20,
      vitalsHistory: generateVitalsHistory(36.8),
      nursingNotes: '注意心率变化，限制活动量',
      handoverRecords: generateHandoverRecords(2)
    },
    {
      id: 3,
      bedNumber: '3床',
      name: '王五',
      age: 72,
      gender: '男',
      admissionDate: '2026-03-15',
      hospitalId: 'H202603003',
      department: '心内科',
      departmentId: 1,
      diagnosis: '急性心肌梗死',
      attendingDoctor: '王主任',
      nurseLevel: '特级',
      isCritical: true,
      allergies: ['磺胺类药物'],
      medicalHistory: ['心肌梗死病史'],
      mewsScore: 3,
      bradenScore: 14,
      fallRisk: 40,
      vitalsHistory: generateVitalsHistory(38.2),
      nursingNotes: '绝对卧床，持续心电监护，严密观察生命体征',
      handoverRecords: generateHandoverRecords(3)
    },
    {
      id: 4,
      bedNumber: '4床',
      name: '赵六',
      age: 55,
      gender: '女',
      admissionDate: '2026-03-18',
      hospitalId: 'H202603004',
      department: '心内科',
      departmentId: 1,
      diagnosis: '高血压2级',
      attendingDoctor: '张医生',
      nurseLevel: '二级',
      isCritical: false,
      allergies: [],
      medicalHistory: ['高血压病史5年'],
      mewsScore: 1,
      bradenScore: 20,
      fallRisk: 15,
      vitalsHistory: generateVitalsHistory(36.5),
      nursingNotes: '规律服药，监测血压',
      handoverRecords: generateHandoverRecords(4)
    },
    {
      id: 5,
      bedNumber: '5床',
      name: '钱七',
      age: 48,
      gender: '男',
      admissionDate: '2026-03-20',
      hospitalId: 'H202603005',
      department: '心内科',
      departmentId: 1,
      diagnosis: '2型糖尿病',
      attendingDoctor: '李主任',
      nurseLevel: '三级',
      isCritical: false,
      allergies: [],
      medicalHistory: ['糖尿病病史2年'],
      mewsScore: 0,
      bradenScore: 19,
      fallRisk: 10,
      vitalsHistory: generateVitalsHistory(36.6),
      nursingNotes: '糖尿病饮食，监测血糖',
      handoverRecords: generateHandoverRecords(5)
    }
  ],
  
  // 神经内科 (科室ID: 2)
  2: [
    {
      id: 11,
      bedNumber: '1床',
      name: '郑十一',
      age: 68,
      gender: '男',
      admissionDate: '2026-03-08',
      hospitalId: 'H202603011',
      department: '神经内科',
      departmentId: 2,
      diagnosis: '脑梗死',
      attendingDoctor: '陈主任',
      nurseLevel: '一级',
      isCritical: true,
      allergies: ['阿司匹林'],
      medicalHistory: ['高血压病史15年', '脑梗死病史'],
      mewsScore: 2,
      bradenScore: 14,
      fallRisk: 45,
      vitalsHistory: generateVitalsHistory(37.2),
      nursingNotes: '卧床护理，预防压疮，注意吞咽功能',
      handoverRecords: generateHandoverRecords(11)
    },
    {
      id: 12,
      bedNumber: '2床',
      name: '王十二',
      age: 55,
      gender: '女',
      admissionDate: '2026-03-10',
      hospitalId: 'H202603012',
      department: '神经内科',
      departmentId: 2,
      diagnosis: '脑出血',
      attendingDoctor: '陈主任',
      nurseLevel: '特级',
      isCritical: true,
      allergies: [],
      medicalHistory: ['高血压病史10年'],
      mewsScore: 3,
      bradenScore: 13,
      fallRisk: 50,
      vitalsHistory: generateVitalsHistory(38.0),
      nursingNotes: '绝对卧床，控制血压，观察意识变化',
      handoverRecords: generateHandoverRecords(12)
    },
    {
      id: 13,
      bedNumber: '3床',
      name: '陈十三',
      age: 42,
      gender: '男',
      admissionDate: '2026-03-12',
      hospitalId: 'H202603013',
      department: '神经内科',
      departmentId: 2,
      diagnosis: '癫痫持续状态',
      attendingDoctor: '林医生',
      nurseLevel: '特级',
      isCritical: true,
      allergies: [],
      medicalHistory: ['癫痫病史5年'],
      mewsScore: 4,
      bradenScore: 18,
      fallRisk: 35,
      vitalsHistory: generateVitalsHistory(37.5),
      nursingNotes: '床边防护，防止癫痫发作时受伤',
      handoverRecords: generateHandoverRecords(13)
    }
  ],
  
  // 呼吸科 (科室ID: 3)
  3: [
    {
      id: 21,
      bedNumber: '1床',
      name: '孙八',
      age: 75,
      gender: '男',
      admissionDate: '2026-03-05',
      hospitalId: 'H202603021',
      department: '呼吸科',
      departmentId: 3,
      diagnosis: '慢性阻塞性肺疾病急性加重',
      attendingDoctor: '周主任',
      nurseLevel: '特级',
      isCritical: true,
      allergies: [],
      medicalHistory: ['COPD病史20年', '吸烟史40年'],
      mewsScore: 3,
      bradenScore: 14,
      fallRisk: 45,
      vitalsHistory: generateVitalsHistory(37.8),
      nursingNotes: '氧疗护理，监测血氧，雾化治疗',
      handoverRecords: generateHandoverRecords(21)
    },
    {
      id: 22,
      bedNumber: '2床',
      name: '周九',
      age: 62,
      gender: '女',
      admissionDate: '2026-03-10',
      hospitalId: 'H202603022',
      department: '呼吸科',
      departmentId: 3,
      diagnosis: '肺炎、呼吸衰竭',
      attendingDoctor: '周主任',
      nurseLevel: '一级',
      isCritical: true,
      allergies: ['头孢类药物'],
      medicalHistory: ['肺炎病史'],
      mewsScore: 2,
      bradenScore: 16,
      fallRisk: 38,
      vitalsHistory: generateVitalsHistory(38.5),
      nursingNotes: '抗感染治疗，氧疗，监测呼吸功能',
      handoverRecords: generateHandoverRecords(22)
    },
    {
      id: 23,
      bedNumber: '3床',
      name: '吴十',
      age: 55,
      gender: '男',
      admissionDate: '2026-03-15',
      hospitalId: 'H202603023',
      department: '呼吸科',
      departmentId: 3,
      diagnosis: '支气管哮喘',
      attendingDoctor: '吴医生',
      nurseLevel: '二级',
      isCritical: false,
      allergies: [],
      medicalHistory: ['哮喘病史10年'],
      mewsScore: 0,
      bradenScore: 19,
      fallRisk: 20,
      vitalsHistory: generateVitalsHistory(36.7),
      nursingNotes: '雾化治疗，避免接触过敏原',
      handoverRecords: generateHandoverRecords(23)
    }
  ],
  
  // 内分泌科 (科室ID: 4)
  4: [
    {
      id: 31,
      bedNumber: '1床',
      name: '郑十',
      age: 45,
      gender: '女',
      admissionDate: '2026-03-12',
      hospitalId: 'H202603031',
      department: '内分泌科',
      departmentId: 4,
      diagnosis: '甲状腺功能亢进',
      attendingDoctor: '黄主任',
      nurseLevel: '二级',
      isCritical: false,
      allergies: [],
      medicalHistory: ['甲亢病史2年'],
      mewsScore: 1,
      bradenScore: 20,
      fallRisk: 15,
      vitalsHistory: generateVitalsHistory(36.8),
      nursingNotes: '抗甲状腺药物治疗，监测心率',
      handoverRecords: generateHandoverRecords(31)
    },
    {
      id: 32,
      bedNumber: '2床',
      name: '王十一',
      age: 58,
      gender: '男',
      admissionDate: '2026-03-08',
      hospitalId: 'H202603032',
      department: '内分泌科',
      departmentId: 4,
      diagnosis: '糖尿病肾病',
      attendingDoctor: '黄主任',
      nurseLevel: '一级',
      isCritical: true,
      allergies: ['磺胺类药物'],
      medicalHistory: ['糖尿病病史15年', '肾病病史3年'],
      mewsScore: 2,
      bradenScore: 17,
      fallRisk: 28,
      vitalsHistory: generateVitalsHistory(36.9),
      nursingNotes: '监测血糖、肾功能，低盐低蛋白饮食',
      handoverRecords: generateHandoverRecords(32)
    }
  ]
}

// ==================== 导出函数 ====================

// 获取患者列表（简化数据）
export function getPatientList(deptId: number): PatientListItem[] {
  const patients = departmentPatients[deptId] || []
  return patients.map(p => ({
    id: p.id,
    bedNumber: p.bedNumber,
    name: p.name,
    age: p.age,
    gender: p.gender,
    admissionDate: p.admissionDate,
    diagnosis: p.diagnosis,
    attendingDoctor: p.attendingDoctor,
    nurseLevel: p.nurseLevel,
    isCritical: p.isCritical,
    allergies: p.allergies,
    mewsScore: p.mewsScore,
    bradenScore: p.bradenScore,
    fallRisk: p.fallRisk,
    latestVitals: p.vitalsHistory[p.vitalsHistory.length - 1]
  }))
}

// 获取患者详情（完整数据）
export function getPatientDetail(id: number): Patient | null {
  for (const patients of Object.values(departmentPatients)) {
    const patient = patients.find(p => p.id === id)
    if (patient) return patient
  }
  return null
}

// 获取所有科室的患者（用于跨科室查询场景）
export function getAllPatients(): Patient[] {
  return Object.values(departmentPatients).flat()
}
```

---

## 4. 页面布局设计

### 4.1 整体布局

```
┌─────────────────────────────────────────────────────────────┐
│  [返回] 患者管理 [心内科]                    [Tab: 表格 | 卡片] │
├─────────────────────────────────────────────────────────────┤
│  搜索过滤栏：床号 | 姓名 | 护理等级 | 风险等级 | 入院日期       │
│             [查询] [重置]                                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  患者列表区（表格或卡片，根据Tab切换）                         │
│                                                             │
│  [分页]                                                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                    │ 点击"查看"
                    ▼
            右侧滑出详情抽屉面板
```

### 4.2 Tab切换

- `<el-tabs>` 组件实现视图切换
- 默认显示表格视图
- 点击"卡片"Tab切换为卡片网格布局

---

## 5. 卡片视图设计

### 5.1 卡片信息内容

**详细卡片包含：**
- 床号 + 重点患者标识（★）
- 姓名、性别、年龄
- 诊断
- 入院日期、主治医生、护理等级
- 最新生命体征（体温、血压）
- 风险评分标签（MEWS、Braden）
- "查看详情"按钮

### 5.2 卡片样式规则

| 条件 | 样式 |
|------|------|
| 重点患者 `isCritical=true` | 左上角 ★ 星标，微红边框 |
| MEWS ≥ 3 | 红色标签 |
| Braden < 14 | 警告色（橙色）标签 |
| 体温 > 38°C 或 < 36°C | 数值标红 |

### 5.3 响应式网格

| 屏幕宽度 | 列数 |
|----------|------|
| ≥ 1200px | 4列 |
| ≥ 992px | 3列 |
| ≥ 768px | 2列 |
| < 768px | 1列 |

---

## 6. 搜索过滤栏设计

### 6.1 筛选项配置

| 字段 | 类型 | 组件 | 说明 |
|------|------|------|------|
| 床号 | 输入框 | `el-input` | 模糊匹配 |
| 姓名 | 输入框 | `el-input` | 模糊匹配 |
| 护理等级 | 下拉选择 | `el-select` | 全部/特级/一级/二级/三级 |
| 风险等级 | 下拉选择 | `el-select` | 全部/高风险/中风险/低风险 |
| 入院日期 | 日期范围 | `el-date-picker type="daterange"` | 可选范围 |

### 6.2 风险等级映射

| 等级 | MEWS分值 |
|------|---------|
| 高风险 | MEWS ≥ 3 |
| 中风险 | MEWS 1-2 |
| 低风险 | MEWS = 0 |

### 6.3 过滤逻辑

```typescript
function filterPatients(patients: PatientListItem[], filter: PatientFilter): PatientListItem[] {
  let result = [...patients]
  
  // 床号模糊匹配
  if (filter.bedNumber) {
    result = result.filter(p => p.bedNumber.includes(filter.bedNumber))
  }
  
  // 姓名模糊匹配
  if (filter.name) {
    result = result.filter(p => p.name.includes(filter.name))
  }
  
  // 护理等级精确匹配
  if (filter.nurseLevel) {
    result = result.filter(p => p.nurseLevel === filter.nurseLevel)
  }
  
  // 风险等级区间过滤
  if (filter.riskLevel === '高风险') {
    result = result.filter(p => p.mewsScore >= 3)
  } else if (filter.riskLevel === '中风险') {
    result = result.filter(p => p.mewsScore >= 1 && p.mewsScore <= 2)
  } else if (filter.riskLevel === '低风险') {
    result = result.filter(p => p.mewsScore === 0)
  }
  
  // 入院日期范围过滤
  if (filter.admissionDateStart) {
    result = result.filter(p => p.admissionDate >= filter.admissionDateStart)
  }
  if (filter.admissionDateEnd) {
    result = result.filter(p => p.admissionDate <= filter.admissionDateEnd)
  }
  
  return result
}
```

---

## 7. 详情抽屉面板设计

### 7.1 抽屉配置

| 属性 | 值 |
|------|-----|
| 组件 | `el-drawer` |
| 方向 | 右侧滑出 |
| 宽度 | 600px |
| 标题 | 患者详情 |

### 7.2 信息模块

抽屉内包含以下模块，可滚动浏览：

#### 模块1：基础信息
- 床号、姓名、性别、年龄
- 住院号、入院日期
- 科室、诊断、主治医生
- 护理等级、重点患者标识

#### 模块2：生命体征趋势
- Tab切换：体温/脉搏/血压/血氧
- ECharts 折线图展示最近7天数据
- 底部显示最新测量值

#### 模块3：风险评估
- MEWS评分（含风险说明）
- Braden评分（含风险说明）
- 跌倒风险评分

#### 模块4：过敏史/病史
- 过敏药物列表
- 既往病史列表

#### 模块5：护理信息
- 特殊护理事项

#### 模块6：交班记录
- 相关交班记录时间线
- "查看完整交班记录"链接

### 7.3 底部操作

- "交班"按钮 → 跳转交班创建页面
- "关闭"按钮 → 关闭抽屉

---

## 8. 技术选型

### 8.1 图表库

选用 **ECharts**：
- 功能强大，满足医疗场景图表需求
- Vue生态成熟（vue-echarts）
- 可复用于后续Dashboard统计图表
- 按需引入控制体积

### 8.2 安装依赖

```bash
npm install echarts vue-echarts
```

### 8.3 ECharts配置

```typescript
// VitalTrendChart.vue

import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])
```

---

## 9. 组件结构设计

### 9.1 文件结构

```
frontend/src/
├── types/
│   └── patient.ts              # 患者数据类型定义（新建）
│
├── mock/
│   └── patient.ts              # 扩展 mock 数据（修改）
│
├── views/
│   └── PatientManagement.vue   # 患者管理页面（修改）
│
├── components/
│   └── VitalTrendChart.vue     # 生命体征趋势图组件（新建）
│
└── styles/
│   └── theme.css               # 主题样式（可能微调）
```

### 9.2 PatientManagement.vue 完整结构

```vue
<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import VitalTrendChart from '@/components/VitalTrendChart.vue'
import { getPatientList, getPatientDetail } from '@/mock/patient'
import type { Patient, PatientListItem, PatientFilter } from '@/types/patient'

// ==================== 状态定义 ====================

const router = useRouter()
const authStore = useAuthStore()

// 视图切换
const activeTab = ref<'table' | 'card'>('table')

// 详情抽屉
const drawerVisible = ref(false)
const selectedPatient = ref<Patient | null>(null)
const currentVitalMetric = ref<'temperature' | 'pulse' | 'bloodPressure' | 'oxygen'>('temperature')

// 搜索过滤
const filterForm = reactive<PatientFilter>({
  bedNumber: '',
  name: '',
  nurseLevel: '',
  riskLevel: '',
  admissionDateStart: '',
  admissionDateEnd: ''
})

// 列表数据
const patientList = ref<PatientListItem[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// ==================== 计算属性 ====================

// 过滤后的患者列表
const filteredPatients = computed(() => {
  let result = [...patientList.value]
  
  if (filterForm.bedNumber) {
    result = result.filter(p => p.bedNumber.includes(filterForm.bedNumber))
  }
  if (filterForm.name) {
    result = result.filter(p => p.name.includes(filterForm.name))
  }
  if (filterForm.nurseLevel) {
    result = result.filter(p => p.nurseLevel === filterForm.nurseLevel)
  }
  if (filterForm.riskLevel === '高风险') {
    result = result.filter(p => p.mewsScore >= 3)
  } else if (filterForm.riskLevel === '中风险') {
    result = result.filter(p => p.mewsScore >= 1 && p.mewsScore <= 2)
  } else if (filterForm.riskLevel === '低风险') {
    result = result.filter(p => p.mewsScore === 0)
  }
  if (filterForm.admissionDateStart) {
    result = result.filter(p => p.admissionDate >= filterForm.admissionDateStart)
  }
  if (filterForm.admissionDateEnd) {
    result = result.filter(p => p.admissionDate <= filterForm.admissionDateEnd)
  }
  
  return result
})

// 分页后的数据
const paginatedPatients = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  pagination.total = filteredPatients.value.length
  return filteredPatients.value.slice(start, end)
})

// ==================== 方法 ====================

// 加载患者列表
const fetchPatients = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    const deptId = authStore.currentDepartmentId
    patientList.value = getPatientList(deptId)
  } catch (error) {
    ElMessage.error('加载患者列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
}

// 重置
const handleReset = () => {
  filterForm.bedNumber = ''
  filterForm.name = ''
  filterForm.nurseLevel = ''
  filterForm.riskLevel = ''
  filterForm.admissionDateStart = ''
  filterForm.admissionDateEnd = ''
  pagination.page = 1
}

// 查看详情
const handleView = (patient: PatientListItem) => {
  selectedPatient.value = getPatientDetail(patient.id)
  drawerVisible.value = true
}

// 交班
const handleHandover = (patient: PatientListItem | Patient | null) => {
  if (patient) {
    router.push(`/handovers/new?patientId=${patient.id}`)
  }
}

// 判断体温异常
const isTempAbnormal = (temp: number) => temp > 38 || temp < 36

// 获取MEWS标签类型
const getMewsTagType = (score: number) => {
  if (score >= 3) return 'danger'
  if (score >= 1) return 'warning'
  return 'success'
}

// 获取Braden标签类型
const getBradenTagType = (score: number) => {
  if (score < 14) return 'danger'
  if (score < 16) return 'warning'
  return 'success'
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchPatients()
})

watch(() => authStore.currentDepartmentId, () => {
  pagination.page = 1
  fetchPatients()
})
</script>

<template>
  <div class="patient-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>患者管理 <span class="dept-badge">{{ authStore.currentDepartmentName }}</span></h1>
      </div>
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="view-tabs">
      <!-- 表格视图 -->
      <el-tab-pane label="表格视图" name="table">
        <!-- 搜索过滤栏 -->
        <el-card class="filter-card">
          <el-form :inline="true" :model="filterForm">
            <el-form-item label="床号">
              <el-input v-model="filterForm.bedNumber" placeholder="床号" style="width: 100px" clearable />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="filterForm.name" placeholder="患者姓名" style="width: 120px" clearable />
            </el-form-item>
            <el-form-item label="护理等级">
              <el-select v-model="filterForm.nurseLevel" placeholder="全部" style="width: 100px" clearable>
                <el-option label="特级" value="特级" />
                <el-option label="一级" value="一级" />
                <el-option label="二级" value="二级" />
                <el-option label="三级" value="三级" />
              </el-select>
            </el-form-item>
            <el-form-item label="风险等级">
              <el-select v-model="filterForm.riskLevel" placeholder="全部" style="width: 100px" clearable>
                <el-option label="高风险" value="高风险" />
                <el-option label="中风险" value="中风险" />
                <el-option label="低风险" value="低风险" />
              </el-select>
            </el-form-item>
            <el-form-item label="入院日期">
              <el-date-picker
                v-model="filterForm.admissionDateStart"
                type="date"
                placeholder="开始日期"
                style="width: 140px"
                value-format="YYYY-MM-DD"
              />
              <span style="margin: 0 8px">~</span>
              <el-date-picker
                v-model="filterForm.admissionDateEnd"
                type="date"
                placeholder="结束日期"
                style="width: 140px"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 表格 -->
        <el-card class="table-card">
          <el-table :data="paginatedPatients" stripe v-loading="loading">
            <el-table-column prop="bedNumber" label="床号" width="80" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="age" label="年龄" width="60" />
            <el-table-column prop="gender" label="性别" width="60" />
            <el-table-column prop="admissionDate" label="入院日期" width="110" />
            <el-table-column prop="diagnosis" label="诊断" min-width="150" />
            <el-table-column prop="attendingDoctor" label="主治医生" width="100" />
            <el-table-column prop="nurseLevel" label="护理等级" width="80" />
            <el-table-column label="过敏史" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.allergies.length" type="warning" size="small">
                  {{ row.allergies.join(', ') }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="风险评分" width="160">
              <template #default="{ row }">
                <div class="risk-scores">
                  <el-tag :type="getMewsTagType(row.mewsScore)" size="small">
                    MEWS: {{ row.mewsScore }}
                  </el-tag>
                  <el-tag :type="getBradenTagType(row.bradenScore)" size="small">
                    Braden: {{ row.bradenScore }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleView(row)">查看</el-button>
                <el-button link type="primary" @click="handleHandover(row)">交班</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 卡片视图 -->
      <el-tab-pane label="卡片视图" name="card">
        <div class="card-grid" v-loading="loading">
          <div
            v-for="patient in paginatedPatients"
            :key="patient.id"
            class="patient-card"
            :class="{ critical: patient.isCritical }"
          >
            <!-- 卡片头部 -->
            <div class="card-header">
              <span class="bed-number">{{ patient.bedNumber }}</span>
              <span v-if="patient.isCritical" class="critical-star">★</span>
              <span class="nurse-level-tag">{{ patient.nurseLevel }}</span>
            </div>

            <!-- 基本信息 -->
            <div class="card-info">
              <div class="name-gender-age">
                {{ patient.name }} {{ patient.gender }} {{ patient.age }}岁
              </div>
              <div class="diagnosis">{{ patient.diagnosis }}</div>
              <div class="meta-info">
                <span>入院: {{ patient.admissionDate }}</span>
                <span>医生: {{ patient.attendingDoctor }}</span>
              </div>
            </div>

            <!-- 生命体征 -->
            <div class="card-vitals">
              <div class="vital-item">
                <span class="vital-label">体温</span>
                <span class="vital-value" :class="{ abnormal: isTempAbnormal(patient.latestVitals.temperature) }">
                  {{ patient.latestVitals.temperature.toFixed(1) }}°C
                </span>
              </div>
              <div class="vital-item">
                <span class="vital-label">血压</span>
                <span class="vital-value">
                  {{ patient.latestVitals.systolicBp }}/{{ patient.latestVitals.diastolicBp }}
                </span>
              </div>
            </div>

            <!-- 风险评分 -->
            <div class="card-risks">
              <el-tag :type="getMewsTagType(patient.mewsScore)" size="small">
                MEWS: {{ patient.mewsScore }}
              </el-tag>
              <el-tag :type="getBradenTagType(patient.bradenScore)" size="small">
                Braden: {{ patient.bradenScore }}
              </el-tag>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button type="primary" size="small" @click="handleView(patient)">查看详情</el-button>
            </div>
          </div>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="患者详情" size="600px" direction="rtl">
      <div v-if="selectedPatient" class="patient-detail">
        <!-- 基础信息 -->
        <div class="detail-section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="床号">{{ selectedPatient.bedNumber }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ selectedPatient.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ selectedPatient.gender }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ selectedPatient.age }}岁</el-descriptions-item>
            <el-descriptions-item label="住院号">{{ selectedPatient.hospitalId }}</el-descriptions-item>
            <el-descriptions-item label="入院日期">{{ selectedPatient.admissionDate }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ selectedPatient.department }}</el-descriptions-item>
            <el-descriptions-item label="主治医生">{{ selectedPatient.attendingDoctor }}</el-descriptions-item>
            <el-descriptions-item label="诊断" :span="2">{{ selectedPatient.diagnosis }}</el-descriptions-item>
            <el-descriptions-item label="护理等级">{{ selectedPatient.nurseLevel }}</el-descriptions-item>
            <el-descriptions-item label="重点患者">
              <el-tag v-if="selectedPatient.isCritical" type="danger" size="small">是</el-tag>
              <span v-else>否</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 生命体征趋势 -->
        <div class="detail-section">
          <div class="section-title">生命体征趋势</div>
          <el-tabs v-model="currentVitalMetric" type="card">
            <el-tab-pane label="体温" name="temperature">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="temperature" />
            </el-tab-pane>
            <el-tab-pane label="脉搏" name="pulse">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="pulse" />
            </el-tab-pane>
            <el-tab-pane label="血压" name="bloodPressure">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="bloodPressure" />
            </el-tab-pane>
            <el-tab-pane label="血氧" name="oxygen">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="oxygen" />
            </el-tab-pane>
          </el-tabs>
          <div class="latest-vitals">
            最新测量: {{ selectedPatient.vitalsHistory[selectedPatient.vitalsHistory.length - 1].time }}
          </div>
        </div>

        <!-- 风险评估 -->
        <div class="detail-section">
          <div class="section-title">风险评估</div>
          <div class="risk-assessment">
            <div class="risk-item">
              <span class="risk-name">MEWS评分</span>
              <el-tag :type="getMewsTagType(selectedPatient.mewsScore)" size="large">
                {{ selectedPatient.mewsScore }}
              </el-tag>
              <span class="risk-desc">
                {{ selectedPatient.mewsScore >= 3 ? '高风险，需立即干预' : 
                   selectedPatient.mewsScore >= 1 ? '中风险，需关注' : '低风险' }}
              </span>
            </div>
            <div class="risk-item">
              <span class="risk-name">Braden评分</span>
              <el-tag :type="getBradenTagType(selectedPatient.bradenScore)" size="large">
                {{ selectedPatient.bradenScore }}
              </el-tag>
              <span class="risk-desc">
                {{ selectedPatient.bradenScore < 14 ? '高风险压疮' : 
                   selectedPatient.bradenScore < 16 ? '中风险压疮' : '低风险压疮' }}
              </span>
            </div>
            <div class="risk-item">
              <span class="risk-name">跌倒风险</span>
              <span class="risk-value">{{ selectedPatient.fallRisk }}分</span>
            </div>
          </div>
        </div>

        <!-- 过敏史/病史 -->
        <div class="detail-section">
          <div class="section-title">过敏史/病史</div>
          <div class="history-section">
            <div class="history-item">
              <span class="history-label">过敏药物:</span>
              <el-tag v-for="allergy in selectedPatient.allergies" :key="allergy" type="warning" class="history-tag">
                {{ allergy }}
              </el-tag>
              <span v-if="!selectedPatient.allergies.length">无</span>
            </div>
            <div class="history-item">
              <span class="history-label">既往病史:</span>
              <span v-for="history in selectedPatient.medicalHistory" :key="history" class="history-text">
                {{ history }}
              </span>
              <span v-if="!selectedPatient.medicalHistory.length">无</span>
            </div>
          </div>
        </div>

        <!-- 护理信息 -->
        <div class="detail-section">
          <div class="section-title">护理信息</div>
          <div class="nuring-notes">{{ selectedPatient.nursingNotes || '无特殊护理事项' }}</div>
        </div>

        <!-- 交班记录 -->
        <div class="detail-section">
          <div class="section-title">交班记录</div>
          <el-timeline>
            <el-timeline-item
              v-for="record in selectedPatient.handoverRecords"
              :key="record.id"
              :timestamp="record.time"
              placement="top"
            >
              <div class="handover-item">
                {{ record.fromDoctor }} → {{ record.toDoctor }}
                <div class="handover-summary">{{ record.summary }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-button link type="primary">查看完整交班记录</el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleHandover(selectedPatient)">交班</el-button>
        <el-button @click="drawerVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
/* 页面头部样式（保留现有） */
.page-header { ... }
.header-left { ... }
.back-btn { ... }
.dept-badge { ... }

/* Tab样式 */
.view-tabs {
  margin-top: 16px;
}

/* 过滤栏样式 */
.filter-card {
  margin-bottom: 16px;
}

/* 表格卡片样式 */
.table-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.risk-scores {
  display: flex;
  gap: 8px;
}

/* 卡片网格样式 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .card-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 992px) {
  .card-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .card-grid { grid-template-columns: 1fr; }
}

/* 单个卡片样式 */
.patient-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
  transition: all 0.2s ease;
}

.patient-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.patient-card.critical {
  border-color: #F56C6C;
  background: rgba(245, 108, 108, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.bed-number {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.critical-star {
  color: #F56C6C;
  font-size: 16px;
}

.nurse-level-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: rgba(255, 179, 102, 0.15);
  color: var(--color-primary-DEFAULT);
  border-radius: 4px;
}

.card-info {
  margin-bottom: 12px;
}

.name-gender-age {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.diagnosis {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.meta-info {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-secondary);
}

.card-vitals {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.vital-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.vital-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.vital-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.vital-value.abnormal {
  color: #F56C6C;
}

.card-risks {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  justify-content: center;
}

/* 详情抽屉样式 */
.patient-detail {
  padding: 0 16px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.latest-vitals {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.risk-assessment {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.risk-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.risk-name {
  font-size: 14px;
  color: var(--text-secondary);
  width: 100px;
}

.risk-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.risk-value {
  font-size: 14px;
  font-weight: 500;
}

.history-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-label {
  font-size: 14px;
  color: var(--text-secondary);
  width: 80px;
}

.history-tag {
  margin-right: 4px;
}

.history-text {
  font-size: 14px;
  color: var(--text-primary);
  margin-right: 8px;
}

.nursing-notes {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.handover-item {
  font-size: 14px;
}

.handover-summary {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
```

### 9.3 VitalTrendChart.vue 完整实现

```vue
<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { VitalRecord } from '@/types/patient'

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

interface Props {
  vitalsHistory: VitalRecord[]
  metric: 'temperature' | 'pulse' | 'bloodPressure' | 'oxygen'
}

const props = defineProps<Props>()

const chartOption = computed(() => {
  const times = props.vitalsHistory.map(v => v.time.slice(5, 16)) // 显示 MM-DD HH:mm
  
  if (props.metric === 'bloodPressure') {
    // 血压需要两条线
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['收缩压', '舒张压'], top: 0 },
      xAxis: { type: 'category', data: times, axisLabel: { fontSize: 10 } },
      yAxis: { type: 'value', name: 'mmHg' },
      grid: { left: '10%', right: '5%', bottom: '15%', top: '20%', containLabel: true },
      series: [
        {
          name: '收缩压',
          type: 'line',
          data: props.vitalsHistory.map(v => v.systolicBp),
          smooth: true,
          lineStyle: { color: '#F56C6C', width: 2 },
          itemStyle: { color: '#F56C6C' }
        },
        {
          name: '舒张压',
          type: 'line',
          data: props.vitalsHistory.map(v => v.diastolicBp),
          smooth: true,
          lineStyle: { color: '#67C23A', width: 2 },
          itemStyle: { color: '#67C23A' }
        }
      ]
    }
  }
  
  // 单指标配置
  const metricConfigs = {
    temperature: { name: '体温(°C)', data: props.vitalsHistory.map(v => v.temperature), color: '#FFB366' },
    pulse: { name: '脉搏(次/分)', data: props.vitalsHistory.map(v => v.pulse), color: '#409EFF' },
    oxygen: { name: '血氧(%)', data: props.vitalsHistory.map(v => v.oxygenSaturation), color: '#E6A23C' }
  }
  
  const config = metricConfigs[props.metric]
  
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: times, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', name: config.name.split('(')[0] },
    grid: { left: '10%', right: '5%', bottom: '15%', top: '10%', containLabel: true },
    series: [
      {
        name: config.name,
        type: 'line',
        data: config.data,
        smooth: true,
        lineStyle: { color: config.color, width: 2 },
        itemStyle: { color: config.color },
        areaStyle: { color: config.color, opacity: 0.1 }
      }
    ]
  }
})
</script>

<template>
  <v-chart :option="chartOption" style="height: 200px" autoresize />
</template>
```

---

## 10. 实现计划

### 10.1 实现步骤（细化版）

| 步骤 | 任务 | 子任务 | 预估时间 |
|------|------|--------|----------|
| 1 | 创建类型定义 | 1.1 定义 Patient/VitalRecord/HandoverRecord 接口<br>1.2 定义 PatientListItem/PatientFilter 接口 | 15分钟 |
| 2 | 扩展 mock 数据 | 2.1 实现 generateVitalsHistory 函数<br>2.2 实现 generateHandoverRecords 函数<br>2.3 完善4个科室的完整患者数据<br>2.4 实现 getPatientList/getPatientDetail 函数 | 30分钟 |
| 3 | 安装 ECharts | 3.1 安装 echarts/vue-echarts<br>3.2 验证依赖安装成功 | 5分钟 |
| 4 | 创建 VitalTrendChart | 4.1 配置 ECharts 按需引入<br>4.2 实现单指标折线图<br>4.3 实现血压双线图<br>4.4 添加样式和响应式 | 45分钟 |
| 5 | 扩展搜索过滤栏 | 5.1 添加护理等级下拉<br>5.2 添加风险等级下拉<br>5.3 添加入院日期范围选择器<br>5.4 实现过滤逻辑 | 20分钟 |
| 6 | 扩展表格列 | 6.1 添加入院日期列<br>6.2 添加主治医生列<br>6.3 添加护理等级列<br>6.4 添加过敏史列 | 15分钟 |
| 7 | Tab切换与卡片视图 | 7.1 实现 el-tabs 结构<br>7.2 设计卡片网格布局<br>7.3 实现单个卡片组件内容<br>7.4 实现响应式网格<br>7.5 实现卡片样式（重点患者标识、异常数值标红） | 40分钟 |
| 8 | 详情抽屉面板 | 8.1 实现 el-drawer 基础结构<br>8.2 实现基础信息模块（el-descriptions）<br>8.3 集成 VitalTrendChart<br>8.4 实现风险评估模块<br>8.5 实现过敏史/病史模块<br>8.6 实现护理信息模块<br>8.7 实现交班记录时间线<br>8.8 实现底部操作按钮 | 60分钟 |
| 9 | 样式调整 | 9.1 调整主题色应用<br>9.2 验证响应式布局<br>9.3 调整间距和字体大小 | 30分钟 |
| 10 | 测试验证 | 10.1 运行 npm run type-check<br>10.2 运行 npm run lint<br>10.3 手动测试各功能点 | 20分钟 |

### 10.2 依赖关系

```
步骤1 (类型定义)
    ↓
步骤2 (mock数据) ← 依赖步骤1的类型
    ↓
步骤4 (VitalTrendChart) ← 依赖步骤1的类型
    ↓
步骤3 (安装ECharts) ← 可与步骤4并行
    ↓
步骤5-8 (PatientManagement) ← 依赖步骤1,2,4
    ↓
步骤9 (样式) ← 依赖步骤5-8完成
    ↓
步骤10 (测试) ← 最终验证
```

### 10.3 可并行执行的任务

- 步骤3（安装ECharts）可与步骤4并行开始
- 步骤5-8中的部分子任务可拆分并行（如搜索栏和表格列扩展）

---

## 11. 验收标准

### 11.1 功能验收

- [ ] 表格视图显示扩展列（入院日期、过敏史、护理等级、主治医生）
- [ ] 卡片视图显示详细卡片信息（含生命体征）
- [ ] Tab切换正常工作，默认显示表格视图
- [ ] 搜索过滤支持所有新增筛选项，组合过滤正确
- [ ] 详情抽屉右侧滑出，宽度600px
- [ ] 详情抽屉显示全部6个模块
- [ ] 生命体征趋势图正常渲染
- [ ] 生命体征趋势图可切换4种指标（体温/脉搏/血压/血氧）
- [ ] 血压趋势图显示双线（收缩压+舒张压）
- [ ] "交班"按钮正确跳转到 `/handovers/new?patientId=X`

### 11.2 样式验收

- [ ] 重点患者卡片左上角显示 ★ 星标
- [ ] 重点患者卡片有微红边框（border-color: #F56C6C）
- [ ] MEWS ≥ 3 显示红色标签
- [ ] MEWS 1-2 显示橙色标签
- [ ] MEWS = 0 显示绿色标签
- [ ] Braden < 14 显示红色标签
- [ ] Braden 14-15 显示橙色标签
- [ ] Braden ≥ 16 显示绿色标签
- [ ] 体温 > 38°C 或 < 36°C 数值标红
- [ ] 卡片网格响应式：大屏4列、中屏3列、小屏2列、移动端1列

### 11.3 技术验收

- [ ] TypeScript 类型检查通过 (`npm run type-check`)
- [ ] ESLint 检查通过 (`npm run lint`)
- [ ] 无运行时报错（浏览器 Console 无红色错误）
- [ ] 所有新增类型定义完整导出
- [ ] Mock 数据函数正确导出和使用

### 11.4 数据验证

- [ ] Mock 数据包含4个科室的完整患者数据
- [ ] 每个科室至少有3-5个患者
- [ ] 每个患者的 vitalsHistory 包含最近7天的28条记录（每天4次）
- [ ] 每个患者的 handoverRecords 包含最近3条交班记录
- [ ] 过敏史和病史数据有实际内容

---

## 12. 备注

### 12.1 易拓展原则

本次设计遵循"易拓展"原则：
- 类型定义独立于文件，便于后续新增字段（只需修改 types/patient.ts）
- Mock数据使用生成器模式，便于批量修改默认值（修改 generateVitalsHistory 等函数）
- 详情抽屉模块化渲染，新增模块只需在模板中添加一个 detail-section 块
- 卡片视图独立渲染，便于调整卡片内容或新增卡片元素

### 12.2 后续对接真实API

Mock数据结构与真实API响应结构保持一致，后续对接时只需：
1. 创建 `api/patient.ts` 封装真实 API 调用
2. 在 PatientManagement.vue 中将 mock 函数调用替换为 API 调用
3. 移除或注释 mock 相关代码

示例转换：
```typescript
// Mock 调用
import { getPatientList } from '@/mock/patient'
const patientList = getPatientList(deptId)

// API 调用（后续）
import { fetchPatients } from '@/api/patient'
const patientList = await fetchPatients(deptId)
```

### 12.3 后续优化建议

以下为非本次范围的后续优化点：
- 生命体征趋势图添加异常阈值线（如体温38°C警戒线）
- 患者详情支持打印功能
- 卡片视图支持拖拽排序
- 添加患者搜索历史记录