# 在院患者管理界面实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完善在院患者管理界面，实现表格/卡片双视图、扩展搜索过滤、详情抽屉面板、生命体征趋势图。

**Architecture:** 单文件增强模式，在现有 PatientManagement.vue 中添加功能，保持易拓展原则。类型定义独立，Mock数据生成器模式，详情抽屉模块化渲染。

**Tech Stack:** Vue 3 + TypeScript + Element Plus + ECharts + vue-echarts

---

## Chunk 1: 类型定义与Mock数据

### Task 1: 创建类型定义文件

**Files:**
- Create: `frontend/src/types/patient.ts`

- [ ] **Step 1: 创建 types 目录并添加 patient.ts 类型定义文件**

```typescript
// frontend/src/types/patient.ts

// 患者完整信息接口
export interface Patient {
  id: number
  bedNumber: string
  name: string
  age: number
  gender: '男' | '女'
  admissionDate: string
  hospitalId: string
  department: string
  departmentId: number
  diagnosis: string
  attendingDoctor: string
  nurseLevel: '特级' | '一级' | '二级' | '三级'
  isCritical: boolean
  allergies: string[]
  medicalHistory: string[]
  mewsScore: number
  bradenScore: number
  fallRisk: number
  vitalsHistory: VitalRecord[]
  nursingNotes: string
  handoverRecords: HandoverRecord[]
}

// 生命体征记录
export interface VitalRecord {
  time: string
  temperature: number
  pulse: number
  respiration: number
  systolicBp: number
  diastolicBp: number
  oxygenSaturation: number
}

// 交班记录（简化）
export interface HandoverRecord {
  id: number
  time: string
  fromDoctor: string
  toDoctor: string
  summary: string
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

- [ ] **Step 2: 运行类型检查验证**

Run: `cd frontend && npm run type-check`
Expected: PASS (新文件不影响现有代码)

- [ ] **Step 3: 提交类型定义**

```bash
git add frontend/src/types/patient.ts
git commit -m "feat: 添加患者管理类型定义"
```

### Task 2: 扩展 Mock 数据

**Files:**
- Modify: `frontend/src/mock/patient.ts`

- [ ] **Step 1: 重写 mock/patient.ts，实现完整 Mock 数据生成器**

```typescript
// frontend/src/mock/patient.ts

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
    
    for (const hour of [8, 12, 16, 20]) {
      records.push({
        time: `${dateStr} ${String(hour).padStart(2, '0')}:00`,
        temperature: Math.max(35, Math.min(42, baseTemp + Math.random() * 1.5 - 0.5)),
        pulse: Math.max(40, Math.min(120, 60 + Math.floor(Math.random() * 40))),
        respiration: Math.max(10, Math.min(30, 16 + Math.floor(Math.random() * 8))),
        systolicBp: Math.max(80, Math.min(200, 110 + Math.floor(Math.random() * 40))),
        diastolicBp: Math.max(50, Math.min(120, 70 + Math.floor(Math.random() * 20))),
        oxygenSaturation: Math.max(85, Math.min(100, 95 + Math.floor(Math.random() * 5)))
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
      id: 1, bedNumber: '1床', name: '张三', age: 65, gender: '男',
      admissionDate: '2026-03-10', hospitalId: 'H202603001',
      department: '心内科', departmentId: 1,
      diagnosis: '2型糖尿病、高血压3级', attendingDoctor: '王主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['青霉素'], medicalHistory: ['高血压病史10年', '糖尿病病史5年'],
      mewsScore: 2, bradenScore: 16, fallRisk: 25,
      vitalsHistory: generateVitalsHistory(37.0),
      nursingNotes: '每日监测血糖4次，注意低血糖症状观察',
      handoverRecords: generateHandoverRecords(1)
    },
    {
      id: 2, bedNumber: '2床', name: '李四', age: 58, gender: '男',
      admissionDate: '2026-03-12', hospitalId: 'H202603002',
      department: '心内科', departmentId: 1,
      diagnosis: '冠心病、心功能不全', attendingDoctor: '李主任',
      nurseLevel: '一级', isCritical: true,
      allergies: [], medicalHistory: ['冠心病病史3年'],
      mewsScore: 1, bradenScore: 18, fallRisk: 20,
      vitalsHistory: generateVitalsHistory(36.8),
      nursingNotes: '注意心率变化，限制活动量',
      handoverRecords: generateHandoverRecords(2)
    },
    {
      id: 3, bedNumber: '3床', name: '王五', age: 72, gender: '男',
      admissionDate: '2026-03-15', hospitalId: 'H202603003',
      department: '心内科', departmentId: 1,
      diagnosis: '急性心肌梗死', attendingDoctor: '王主任',
      nurseLevel: '特级', isCritical: true,
      allergies: ['磺胺类药物'], medicalHistory: ['心肌梗死病史'],
      mewsScore: 3, bradenScore: 14, fallRisk: 40,
      vitalsHistory: generateVitalsHistory(38.2),
      nursingNotes: '绝对卧床，持续心电监护，严密观察生命体征',
      handoverRecords: generateHandoverRecords(3)
    },
    {
      id: 4, bedNumber: '4床', name: '赵六', age: 55, gender: '女',
      admissionDate: '2026-03-18', hospitalId: 'H202603004',
      department: '心内科', departmentId: 1,
      diagnosis: '高血压2级', attendingDoctor: '张医生',
      nurseLevel: '二级', isCritical: false,
      allergies: [], medicalHistory: ['高血压病史5年'],
      mewsScore: 1, bradenScore: 20, fallRisk: 15,
      vitalsHistory: generateVitalsHistory(36.5),
      nursingNotes: '规律服药，监测血压',
      handoverRecords: generateHandoverRecords(4)
    },
    {
      id: 5, bedNumber: '5床', name: '钱七', age: 48, gender: '男',
      admissionDate: '2026-03-20', hospitalId: 'H202603005',
      department: '心内科', departmentId: 1,
      diagnosis: '2型糖尿病', attendingDoctor: '李主任',
      nurseLevel: '三级', isCritical: false,
      allergies: [], medicalHistory: ['糖尿病病史2年'],
      mewsScore: 0, bradenScore: 19, fallRisk: 10,
      vitalsHistory: generateVitalsHistory(36.6),
      nursingNotes: '糖尿病饮食，监测血糖',
      handoverRecords: generateHandoverRecords(5)
    }
  ],
  
  // 神经内科 (科室ID: 2)
  2: [
    {
      id: 11, bedNumber: '1床', name: '郑十一', age: 68, gender: '男',
      admissionDate: '2026-03-08', hospitalId: 'H202603011',
      department: '神经内科', departmentId: 2,
      diagnosis: '脑梗死', attendingDoctor: '陈主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['阿司匹林'], medicalHistory: ['高血压病史15年', '脑梗死病史'],
      mewsScore: 2, bradenScore: 14, fallRisk: 45,
      vitalsHistory: generateVitalsHistory(37.2),
      nursingNotes: '卧床护理，预防压疮，注意吞咽功能',
      handoverRecords: generateHandoverRecords(11)
    },
    {
      id: 12, bedNumber: '2床', name: '王十二', age: 55, gender: '女',
      admissionDate: '2026-03-10', hospitalId: 'H202603012',
      department: '神经内科', departmentId: 2,
      diagnosis: '脑出血', attendingDoctor: '陈主任',
      nurseLevel: '特级', isCritical: true,
      allergies: [], medicalHistory: ['高血压病史10年'],
      mewsScore: 3, bradenScore: 13, fallRisk: 50,
      vitalsHistory: generateVitalsHistory(38.0),
      nursingNotes: '绝对卧床，控制血压，观察意识变化',
      handoverRecords: generateHandoverRecords(12)
    },
    {
      id: 13, bedNumber: '3床', name: '陈十三', age: 42, gender: '男',
      admissionDate: '2026-03-12', hospitalId: 'H202603013',
      department: '神经内科', departmentId: 2,
      diagnosis: '癫痫持续状态', attendingDoctor: '林医生',
      nurseLevel: '特级', isCritical: true,
      allergies: [], medicalHistory: ['癫痫病史5年'],
      mewsScore: 4, bradenScore: 18, fallRisk: 35,
      vitalsHistory: generateVitalsHistory(37.5),
      nursingNotes: '床边防护，防止癫痫发作时受伤',
      handoverRecords: generateHandoverRecords(13)
    }
  ],
  
  // 呼吸科 (科室ID: 3)
  3: [
    {
      id: 21, bedNumber: '1床', name: '孙八', age: 75, gender: '男',
      admissionDate: '2026-03-05', hospitalId: 'H202603021',
      department: '呼吸科', departmentId: 3,
      diagnosis: '慢性阻塞性肺疾病急性加重', attendingDoctor: '周主任',
      nurseLevel: '特级', isCritical: true,
      allergies: [], medicalHistory: ['COPD病史20年', '吸烟史40年'],
      mewsScore: 3, bradenScore: 14, fallRisk: 45,
      vitalsHistory: generateVitalsHistory(37.8),
      nursingNotes: '氧疗护理，监测血氧，雾化治疗',
      handoverRecords: generateHandoverRecords(21)
    },
    {
      id: 22, bedNumber: '2床', name: '周九', age: 62, gender: '女',
      admissionDate: '2026-03-10', hospitalId: 'H202603022',
      department: '呼吸科', departmentId: 3,
      diagnosis: '肺炎、呼吸衰竭', attendingDoctor: '周主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['头孢类药物'], medicalHistory: ['肺炎病史'],
      mewsScore: 2, bradenScore: 16, fallRisk: 38,
      vitalsHistory: generateVitalsHistory(38.5),
      nursingNotes: '抗感染治疗，氧疗，监测呼吸功能',
      handoverRecords: generateHandoverRecords(22)
    },
    {
      id: 23, bedNumber: '3床', name: '吴十', age: 55, gender: '男',
      admissionDate: '2026-03-15', hospitalId: 'H202603023',
      department: '呼吸科', departmentId: 3,
      diagnosis: '支气管哮喘', attendingDoctor: '吴医生',
      nurseLevel: '二级', isCritical: false,
      allergies: [], medicalHistory: ['哮喘病史10年'],
      mewsScore: 0, bradenScore: 19, fallRisk: 20,
      vitalsHistory: generateVitalsHistory(36.7),
      nursingNotes: '雾化治疗，避免接触过敏原',
      handoverRecords: generateHandoverRecords(23)
    }
  ],
  
  // 内分泌科 (科室ID: 4)
  4: [
    {
      id: 31, bedNumber: '1床', name: '郑十', age: 45, gender: '女',
      admissionDate: '2026-03-12', hospitalId: 'H202603031',
      department: '内分泌科', departmentId: 4,
      diagnosis: '甲状腺功能亢进', attendingDoctor: '黄主任',
      nurseLevel: '二级', isCritical: false,
      allergies: [], medicalHistory: ['甲亢病史2年'],
      mewsScore: 1, bradenScore: 20, fallRisk: 15,
      vitalsHistory: generateVitalsHistory(36.8),
      nursingNotes: '抗甲状腺药物治疗，监测心率',
      handoverRecords: generateHandoverRecords(31)
    },
    {
      id: 32, bedNumber: '2床', name: '王十一', age: 58, gender: '男',
      admissionDate: '2026-03-08', hospitalId: 'H202603032',
      department: '内分泌科', departmentId: 4,
      diagnosis: '糖尿病肾病', attendingDoctor: '黄主任',
      nurseLevel: '一级', isCritical: true,
      allergies: ['磺胺类药物'], medicalHistory: ['糖尿病病史15年', '肾病病史3年'],
      mewsScore: 2, bradenScore: 17, fallRisk: 28,
      vitalsHistory: generateVitalsHistory(36.9),
      nursingNotes: '监测血糖、肾功能，低盐低蛋白饮食',
      handoverRecords: generateHandoverRecords(32)
    }
  ]
}

// ==================== 导出函数 ====================

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
    latestVitals: p.vitalsHistory[p.vitalsHistory.length - 1] || {
      time: '', temperature: 36.5, pulse: 70, respiration: 16,
      systolicBp: 120, diastolicBp: 80, oxygenSaturation: 98
    }
  }))
}

export function getPatientDetail(id: number): Patient | null {
  for (const patients of Object.values(departmentPatients)) {
    const patient = patients.find(p => p.id === id)
    if (patient) return patient
  }
  return null
}

export function getAllPatients(): Patient[] {
  return Object.values(departmentPatients).flat()
}
```

- [ ] **Step 2: 运行类型检查验证**

Run: `cd frontend && npm run type-check`
Expected: PASS

- [ ] **Step 3: 提交 Mock 数据**

```bash
git add frontend/src/mock/patient.ts
git commit -m "feat: 扩展患者管理Mock数据生成器"
```

---

## Chunk 2: 安装依赖与图表组件

### Task 3: 安装 ECharts 依赖

**Files:**
- Modify: `frontend/package.json`

- [ ] **Step 1: 安装 echarts 和 vue-echarts**

Run: `cd frontend && npm install echarts vue-echarts`
Expected: 依赖安装成功

- [ ] **Step 2: 验证安装**

Run: `cd frontend && npm ls echarts vue-echarts`
Expected: 显示已安装版本

### Task 4: 创建 VitalTrendChart 组件

**Files:**
- Create: `frontend/src/components/VitalTrendChart.vue`

- [ ] **Step 1: 创建 VitalTrendChart.vue 组件**

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

const hasData = computed(() => props.vitalsHistory && props.vitalsHistory.length >= 2)

const chartOption = computed(() => {
  if (!hasData.value) return null
  
  const times = props.vitalsHistory.map(v => v.time.slice(5, 16))
  
  if (props.metric === 'bloodPressure') {
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
  <div v-if="hasData && chartOption">
    <v-chart :option="chartOption" style="height: 200px" autoresize />
  </div>
  <div v-else class="empty-chart">
    <el-empty description="暂无足够数据生成趋势图" :image-size="80" />
  </div>
</template>

<style scoped>
.empty-chart {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
```

- [ ] **Step 2: 运行类型检查验证**

Run: `cd frontend && npm run type-check`
Expected: PASS

- [ ] **Step 3: 提交图表组件**

```bash
git add frontend/src/components/VitalTrendChart.vue frontend/package.json frontend/package-lock.json
git commit -m "feat: 添加生命体征趋势图组件"
```

---

## Chunk 3: PatientManagement.vue 核心改造

### Task 5: 重写 PatientManagement.vue

**Files:**
- Modify: `frontend/src/views/PatientManagement.vue`

- [ ] **Step 1: 完整重写 PatientManagement.vue（包含所有功能）**

完整代码见设计文档 Section 9.2，包含：
- Tab切换（表格/卡片视图）
- 扩展搜索过滤栏
- 表格列扩展
- 卡片网格布局
- 详情抽屉面板（6个模块）
- VitalTrendChart集成

由于代码较长（约500行），分步实现：

**Part A: Script 部分（状态、计算属性、方法）**

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
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

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

// 加载患者列表
const fetchPatients = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    const deptId = authStore.currentDepartmentId
    patientList.value = getPatientList(deptId || 1)
  } catch (error) {
    ElMessage.error('加载患者列表失败')
    patientList.value = []
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
  const detail = getPatientDetail(patient.id)
  if (!detail) {
    ElMessage.warning('患者数据不存在')
    return
  }
  selectedPatient.value = detail
  drawerVisible.value = true
}

// 交班
const handleHandover = (patient: PatientListItem | Patient | null) => {
  if (!patient) {
    ElMessage.warning('请先选择患者')
    return
  }
  router.push(`/handovers/new?patientId=${patient.id}`)
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

onMounted(() => {
  fetchPatients()
})

watch(() => authStore.currentDepartmentId, () => {
  pagination.page = 1
  fetchPatients()
})
</script>
```

**Part B: Template 部分（完整模板）**

见下一步实现。

- [ ] **Step 2: 运行类型检查验证**

Run: `cd frontend && npm run type-check`
Expected: PASS

- [ ] **Step 3: 提交核心改造**

```bash
git add frontend/src/views/PatientManagement.vue
git commit -m "feat: 完善患者管理界面核心功能

- 添加Tab切换（表格/卡片视图）
- 扩展搜索过滤（护理等级、风险等级、入院日期）
- 扩展表格列（入院日期、主治医生、护理等级、过敏史）
- 添加卡片网格布局
- 添加详情抽屉面板（6个信息模块）
- 集成生命体征趋势图"
```

---

## Chunk 4: 验证与测试

### Task 6: 最终验证

- [ ] **Step 1: 运行类型检查**

Run: `cd frontend && npm run type-check`
Expected: PASS，无错误

- [ ] **Step 2: 运行 lint 检查**

Run: `cd frontend && npm run lint`
Expected: PASS，无错误

- [ ] **Step 3: 启动开发服务器验证**

Run: `cd frontend && npm run dev`
Expected: 服务器启动成功，访问 http://localhost:3000/patients

- [ ] **Step 4: 手动验证功能点**

在浏览器中验证：
- [ ] 表格视图显示扩展列
- [ ] Tab切换到卡片视图正常
- [ ] 搜索过滤功能正常
- [ ] 点击"查看"打开详情抽屉
- [ ] 详情抽屉显示所有6个模块
- [ ] 生命体征趋势图正常渲染
- [ ] "交班"按钮跳转正确

- [ ] **Step 5: 最终提交**

```bash
git add -A
git commit -m "chore: 最终验证并提交患者管理界面完善"
```

---

## 执行说明

**执行顺序：**
1. Chunk 1 → Task 1-2（类型定义 + Mock数据）
2. Chunk 2 → Task 3-4（安装依赖 + 图表组件）
3. Chunk 3 → Task 5（PatientManagement.vue核心改造）
4. Chunk 4 → Task 6（验证与测试）

**依赖关系：**
- Task 2 依赖 Task 1（Mock数据引用类型）
- Task 4 依赖 Task 1 + Task 3（图表组件引用类型和ECharts）
- Task 5 依赖 Task 1-4（PatientManagement引用所有）
- Task 6 依赖 Task 5（验证整体功能）