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
  admissionDate: string           // 入院日期
  hospitalId: string              // 住院号
  department: string              // 科室
  departmentId: number            // 科室ID
  diagnosis: string               // 诊断
  attendingDoctor: string         // 主治医生
  nurseLevel: string              // 护理等级（特级/一级/二级/三级）
  isCritical: boolean             // 是否重点患者
  
  // 过敏史/病史
  allergies: string[]             // 过敏药物列表
  medicalHistory: string[]        // 既往病史
  
  // 风险评估
  mewsScore: number               // MEWS评分 (0-5)
  bradenScore: number             // Braden评分 (15-23)
  fallRisk: number                // 跌倒风险评分
  
  // 生命体征（历史记录）
  vitalsHistory: VitalRecord[]    // 生命体征历史记录
  
  // 护理信息
  nursingNotes: string            // 特殊护理事项
  
  // 关联交班记录
  handoverIds: number[]           // 相关交班记录ID列表
}

// 生命体征记录
export interface VitalRecord {
  time: string                    // 测量时间
  temperature: number             // 体温 (°C)
  pulse: number                   // 脉搏 (次/分)
  respiration: number             // 呼吸 (次/分)
  systolicBp: number              // 收缩压 (mmHg)
  diastolicBp: number             // 舒张压 (mmHg)
  oxygenSaturation: number        // 血氧饱和度 (%)
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
  nurseLevel: string
  isCritical: boolean
  allergies: string[]
  mewsScore: number
  bradenScore: number
  fallRisk: number
  // 最新生命体征
  latestVitals: VitalRecord
}
```

### 3.2 Mock 数据生成器

扩展 `mock/patient.ts`，使用生成器模式：

```typescript
// mock/patient.ts

import { Patient, VitalRecord, PatientListItem } from '@/types/patient'

// 生成生命体征历史记录（最近7天）
function generateVitalsHistory(): VitalRecord[] {
  const records: VitalRecord[] = []
  const now = new Date()
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    
    // 每天4次测量（08:00, 12:00, 16:00, 20:00）
    for (let hour of [8, 12, 16, 20]) {
      records.push({
        time: `${date.toISOString().slice(0, 10)} ${hour}:00`,
        temperature: 36 + Math.random() * 1.5,
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

// 各科室患者数据
const departmentPatients: Record<number, Patient[]> = {
  1: [...],  // 心内科
  2: [...],  // 神经内科
  3: [...],  // 呼吸科
  4: [...]   // 内分泌科
}

export function getPatientList(deptId: number): PatientListItem[] {
  // 返回简化列表数据
}

export function getPatientDetail(id: number): Patient | null {
  // 返回完整详情数据
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
| 入院日期 | 日期范围 | `el-date-picker` | 可选范围 |

### 6.2 风险等级映射

| 等级 | MEWS分值 |
|------|---------|
| 高风险 | MEWS ≥ 3 |
| 中风险 | MEWS 1-2 |
| 低风险 | MEWS = 0 |

### 6.3 过滤逻辑

- 多条件组合过滤（AND逻辑）
- 空值条件不参与过滤
- 护理等级精确匹配
- 风险等级按 MEWS 分值区间过滤
- 入院日期范围过滤

---

## 7. 详情抽屉面板设计

### 7.1 抽屉配置

| 属性 | 值 |
|------|-----|
| 组件 | `el-drawer` |
| 方向 | 右侧滑出 |
| 宽度 | 600px（可拖拽调整） |
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
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])
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

### 9.2 PatientManagement.vue 内部结构

```vue
<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import VitalTrendChart from '@/components/VitalTrendChart.vue'
import type { Patient, PatientListItem } from '@/types/patient'

// 状态
const activeTab = ref<'table' | 'card'>('table')
const drawerVisible = ref(false)
const selectedPatient = ref<Patient | null>(null)
const currentVitalMetric = ref<'temperature' | 'pulse' | 'bloodPressure' | 'oxygen'>('temperature')

// 搜索过滤
const filterForm = reactive({
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
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

// 方法
const handleView = (patient: PatientListItem) => {
  selectedPatient.value = getPatientDetail(patient.id)
  drawerVisible.value = true
}

const handleHandover = (patient: PatientListItem) => {
  router.push(`/handovers/new?patientId=${patient.id}`)
}
</script>

<template>
  <div class="patient-management">
    <!-- 页面头部 -->
    <div class="page-header">
      ...
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="view-tabs">
      <el-tab-pane label="表格视图" name="table">
        <!-- 搜索过滤栏 -->
        <el-card class="filter-card">
          <el-form :inline="true" :model="filterForm">
            <el-form-item label="床号">...</el-form-item>
            <el-form-item label="姓名">...</el-form-item>
            <el-form-item label="护理等级">...</el-form-item>
            <el-form-item label="风险等级">...</el-form-item>
            <el-form-item label="入院日期">...</el-form-item>
          </el-form>
        </el-card>

        <!-- 表格 -->
        <el-card class="table-card">
          <el-table :data="patientList">
            <el-table-column prop="bedNumber" label="床号" />
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="age" label="年龄" />
            <el-table-column prop="gender" label="性别" />
            <el-table-column prop="admissionDate" label="入院日期" />
            <el-table-column prop="diagnosis" label="诊断" />
            <el-table-column prop="attendingDoctor" label="主治医生" />
            <el-table-column prop="nurseLevel" label="护理等级" />
            <el-table-column label="过敏史">...</el-table-column>
            <el-table-column label="风险评分">...</el-table-column>
            <el-table-column label="操作">
              <el-button @click="handleView">查看</el-button>
              <el-button @click="handleHandover">交班</el-button>
            </el-table-column>
          </el-table>
          <el-pagination ... />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="卡片视图" name="card">
        <!-- 卡片网格 -->
        <div class="card-grid">
          <div v-for="patient in patientList" class="patient-card" :class="{ critical: patient.isCritical }">
            <!-- 卡片内容 -->
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="患者详情" size="600px">
      <div v-if="selectedPatient" class="patient-detail">
        <!-- 基础信息 -->
        <div class="detail-section">...</div>
        
        <!-- 生命体征趋势 -->
        <div class="detail-section">
          <el-tabs v-model="currentVitalMetric">
            <el-tab-pane label="体温" name="temperature">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="temperature" />
            </el-tab-pane>
            <el-tab-pane label="脉搏" name="pulse">...</el-tab-pane>
            <el-tab-pane label="血压" name="bloodPressure">...</el-tab-pane>
            <el-tab-pane label="血氧" name="oxygen">...</el-tab-pane>
          </el-tabs>
        </div>
        
        <!-- 风险评估 -->
        <div class="detail-section">...</div>
        
        <!-- 过敏史/病史 -->
        <div class="detail-section">...</div>
        
        <!-- 护理信息 -->
        <div class="detail-section">...</div>
        
        <!-- 交班记录 -->
        <div class="detail-section">...</div>
      </div>
      
      <template #footer>
        <el-button @click="handleHandover(selectedPatient)">交班</el-button>
        <el-button @click="drawerVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>
```

### 9.3 VitalTrendChart.vue 组件

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

// 根据metric生成图表配置
const chartOption = computed(() => {
  const times = props.vitalsHistory.map(v => v.time.slice(5))  // 显示 MM-DD HH:mm
  
  // 不同指标的配置
  const metricConfig = {
    temperature: { name: '体温(°C)', data: props.vitalsHistory.map(v => v.temperature), color: '#FFB366' },
    pulse: { name: '脉搏(次/分)', data: props.vitalsHistory.map(v => v.pulse), color: '#409EFF' },
    bloodPressure: {
      name: '血压(mmHg)',
      series: [
        { name: '收缩压', data: props.vitalsHistory.map(v => v.systolicBp), color: '#F56C6C' },
        { name: '舒张压', data: props.vitalsHistory.map(v => v.diastolicBp), color: '#67C23A' }
      ]
    },
    oxygen: { name: '血氧(%)', data: props.vitalsHistory.map(v => v.oxygenSaturation), color: '#E6A23C' }
  }
  
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: times },
    yAxis: { type: 'value' },
    series: [...],  // 根据metric生成
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
  }
})
</script>

<template>
  <v-chart :option="chartOption" style="height: 200px" autoresize />
</template>
```

---

## 10. 实现计划

### 10.1 实现步骤

| 步骤 | 任务 | 预估时间 |
|------|------|----------|
| 1 | 创建类型定义文件 `types/patient.ts` | 15分钟 |
| 2 | 扩展 mock 数据 `mock/patient.ts` | 30分钟 |
| 3 | 安装 ECharts 依赖 | 5分钟 |
| 4 | 创建 VitalTrendChart 组件 | 45分钟 |
| 5 | 修改 PatientManagement.vue - 搜索过滤栏扩展 | 20分钟 |
| 6 | 修改 PatientManagement.vue - 表格列扩展 | 15分钟 |
| 7 | 修改 PatientManagement.vue - Tab切换与卡片视图 | 40分钟 |
| 8 | 修改 PatientManagement.vue - 详情抽屉面板 | 60分钟 |
| 9 | 样式调整与响应式适配 | 30分钟 |
| 10 | 类型检查与测试 | 20分钟 |

### 10.2 依赖关系

```
步骤1 (类型定义)
    ↓
步骤2 (mock数据) ← 依赖步骤1的类型
    ↓
步骤4 (VitalTrendChart) ← 依赖步骤1的类型
    ↓
步骤5-8 (PatientManagement) ← 依赖步骤1,2,4
    ↓
步骤9 (样式)
    ↓
步骤10 (测试)
```

---

## 11. 验收标准

### 11.1 功能验收

- [ ] 表格视图显示扩展列（入院日期、过敏史、护理等级、主治医生）
- [ ] 卡片视图显示详细卡片信息
- [ ] Tab切换正常工作
- [ ] 搜索过滤支持所有新增筛选项
- [ ] 详情抽屉右侧滑出，显示所有6个模块
- [ ] 生命体征趋势图正常渲染，可切换指标
- [ ] "交班"按钮跳转正确

### 11.2 样式验收

- [ ] 重点患者卡片有星标和红边框
- [ ] 风险评分标签颜色正确（高风险红、中风险橙、低风险绿）
- [ ] 体温异常数值标红
- [ ] 卡片网格响应式布局正确

### 11.3 技术验收

- [ ] TypeScript 类型检查通过 (`npm run type-check`)
- [ ] ESLint 检查通过 (`npm run lint`)
- [ ] 无运行时报错

---

## 12. 备注

### 12.1 易拓展原则

本次设计遵循"易拓展"原则：
- 类型定义独立于文件，便于后续新增字段
- Mock数据使用生成器模式，便于批量修改默认值
- 详情抽屉模块化渲染，新增模块只需添加渲染块

### 12.2 后续对接真实API

Mock数据结构与真实API响应结构保持一致，后续对接时只需：
1. 将 Mock 函数替换为真实 API 调用
2. 移除 `USE_MOCK` 标志