# 交班报表打印功能 - 设计文档

## 功能概述

在医生发起交班并核对信息后，支持生成可打印的交班报表，用于签字存档或交接核对。

**打印场景**:
1. **交班前预览打印** - 发起医生在提交前打印核对
2. **交班确认打印** - 双方医生确认后打印签字存档
3. **历史记录打印** - 从已完成的交班记录中重新打印

## 用户流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           打印功能用户流程                                   │
└─────────────────────────────────────────────────────────────────────────────┘

场景一：交班前预览打印（发起医生）

   CreateHandoverDialog
          │
          │ 填写患者病情、选择接班医生
          ▼
   ┌──────────────┐
   │  预览交班报告  │ ◄── 点击「预览报告」按钮
   │  ReportPreview │
   └──────┬───────┘
          │
          │ 查看完整报告内容
          ▼
   ┌──────────────┐      ┌──────────────┐
   │   打印预览    │ ───► │  浏览器打印   │ ◄── 点击「打印」按钮
   │  print.css   │      │   Ctrl+P     │
   └──────────────┘      └──────────────┘
          │
          ▼
   ┌──────────────┐
   │  确认发起交班  │
   └──────────────┘


场景二：接班确认后打印（双方医生）

   HandoverManagement
          │
          │ 查看交班详情
          ▼
   ┌──────────────┐
   │   接班确认    │
   └──────┬───────┘
          │
          │ 确认接班
          ▼
   ┌──────────────┐
   │  打印签字版   │ ◄── 点击「打印签字版」按钮
   │  含双方签名   │
   └──────────────┘


场景三：历史记录打印（归档）

   HandoverManagement
          │
          │ 选择已完成的交班记录
          ▼
   ┌──────────────┐
   │   查看详情    │
   └──────┬───────┘
          │
          │ 点击「打印报告」
          ▼
   ┌──────────────┐
   │   打印预览    │
   └──────────────┘
```

## 界面设计

### 1. CreateHandoverDialog 新增按钮

在发起交班对话框底部添加「预览报告」按钮:

```
┌─────────────────────────────────────────────────────────────────┐
│ 发起交班                                                        │
├─────────────────────────────────────────────────────────────────┤
│ [患者列表区域...]                                               │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │ 语音录入                                                 │  │
│  └─────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  [取消]              [预览报告]  [确认发起交班 ✓]              │
└─────────────────────────────────────────────────────────────────┘
```

按钮样式:
- 「预览报告」: 次要按钮，淡橙色边框，白色背景
- 位置: 取消按钮右侧，确认按钮左侧

### 2. ReportPreview 预览弹窗 (新增组件)

全屏弹窗，展示完整交班报告:

```
┌────────────────────────────────────────────────────────────────────────────┐
│ 📋 交班报告预览                                              [X]          │
├────────────────────────────────────────────────────────────────────────────┤
│ ┌────────────────────────────────────────────────────────────────────────┐ │
│ │                                                                        │ │
│ │  北京大学国际医院                                                        │ │
│ │  科室交班报告                                                            │ │
│ │                                                                        │ │
│ │  ────────────────────────────────────────────────────────────────────  │ │
│ │                                                                        │ │
│ │  科室: 心内科          日期: 2026-03-27         班次: 白班              │ │
│ │  交班医生: 张医生      接班医生: 李医生         状态: 待交班            │ │
│ │                                                                        │ │
│ │  ────────────────────────────────────────────────────────────────────  │ │
│ │                                                                        │ │
│ │  【科室统计】                                                            │ │
│ │  患者总数: 8人    入院: 1    转出: 1    出院: 1    转入: 0               │ │
│ │  手术: 0          死亡: 0              病危: 2                         │ │
│ │                                                                        │ │
│ │  ────────────────────────────────────────────────────────────────────  │ │
│ │                                                                        │ │
│ │  【重点患者详情】                                                         │ │
│ │                                                                        │ │
│ │  1床 张三                                                                │ │
│ │    诊断: 2型糖尿病、高血压3级                                            │ │
│ │    病情: 主诉头痛、恶心、呕吐、视物模糊1周...                               │ │
│ │    MEWS评分: 2分 (中风险)                                               │ │
│ │    Braden评分: 16分                                                     │ │
│ │                                                                        │ │
│ │  2床 李四                                                                │ │
│ │    诊断: 冠心病、心功能不全                                              │ │
│ │    ...                                                                 │ │
│ │                                                                        │ │
│ │  ────────────────────────────────────────────────────────────────────  │ │
│ │                                                                        │ │
│ │  【SBAR交班摘要】                                                         │ │
│ │                                                                        │ │
│ │  S - 现状:                                                               │ │
│ │      心内科今日共8名患者，重点患者3名。张三（1床）血糖波动较大...           │ │
│ │                                                                        │ │
│ │  B - 背景:                                                               │ │
│ │      张三：入院日期2026-03-15，今日血糖最高15.2mmol/L...                   │ │
│ │                                                                        │ │
│ │  A - 评估:                                                               │ │
│ │      张三：血糖控制不佳，MEWS评分2分。李四病情平稳...                       │ │
│ │                                                                        │ │
│ │  R - 建议:                                                               │ │
│ │      张三：监测Q4H血糖，内分泌科会诊。李四：继续监测血压...                  │ │
│ │                                                                        │ │
│ │  ────────────────────────────────────────────────────────────────────  │ │
│ │                                                                        │ │
│ │  【待办事项】                                                            │ │
│ │  □ 明日空腹血糖复查          截止时间: 2026-03-28 06:00                 │ │
│ │  □ 内分泌科会诊              截止时间: 2026-03-28 14:00                 │ │
│ │                                                                        │ │
│ │  ────────────────────────────────────────────────────────────────────  │ │
│ │                                                                        │ │
│ │  交班医生签名: _________________    接班医生签名: _________________      │ │
│ │                                                                        │ │
│ │                                                                        │ │
│ └────────────────────────────────────────────────────────────────────────┘ │
│                                                                            │
│  [返回编辑]         [导出PDF]  [打印 🖨️]                                  │
└────────────────────────────────────────────────────────────────────────────┘
```

### 3. HandoverManagement 详情页新增打印入口

在交班详情弹窗的底部添加「打印报告」按钮:

```
┌────────────────────────────────────────────────────────────────────────────┐
│ 交班详情 - 心内科 白班                                       [X]          │
├────────────────────────────────────────────────────────────────────────────┤
│ [交班内容展示...]                                                          │
│                                                                            │
├────────────────────────────────────────────────────────────────────────────┤
│ [关闭]                            [打印报告 🖨️]  [确认接班 ✓]             │
└────────────────────────────────────────────────────────────────────────────┘
```

## 数据结构 (Mock数据)

### 打印报表数据结构

```typescript
// 打印报表数据接口
interface PrintableReport {
  // 基本信息
  id: number
  departmentId: number
  departmentName: string
  handoverDate: string          // 交班日期
  shift: '白班' | '夜班' | '大夜班'
  fromDoctorId: number
  fromDoctorName: string
  toDoctorId: number
  toDoctorName: string
  status: 'DRAFT' | 'PENDING' | 'TRANSFERRING' | 'COMPLETED'

  // 科室统计
  stats: {
    totalPatients: number       // 患者总数
    admission: number           // 入院
    transferOut: number         // 转出
    discharge: number           // 出院
    transferIn: number          // 转入
    death: number               // 死亡
    surgery: number             // 手术
    criticalCount: number       // 病危/重点患者数
  }

  // 患者详情列表
  patients: PrintablePatient[]

  // SBAR交班报告内容
  reportContent: {
    situation: string           // 现状
    background: string          // 背景
    assessment: string          // 评估
    recommendation: string      // 建议
  }

  // 待办事项
  todos: PrintableTodo[]

  // 打印元数据
  printMetadata: {
    generatedAt: string         // 生成时间
    version: number             // 版本号
    printedAt?: string          // 打印时间
    isSigned: boolean           // 是否已签名
  }
}

// 打印患者详情
interface PrintablePatient {
  id: number
  bedNumber: string
  name: string
  age?: number
  gender?: '男' | '女'
  diagnosis: string
  condition: string           // 病情描述
  isCritical: boolean         // 是否重点患者

  // 生命体征 (最新)
  vitals?: {
    temperature?: number
    heartRate?: number
    bloodPressure?: string    // 如 "120/80"
    respiratoryRate?: number
    spo2?: number
  }

  // 风险评估
  riskScores: {
    mews?: number             // MEWS评分
    mewsLevel: 'low' | 'moderate' | 'high'
    braden?: number           // Braden评分
    bradenLevel: 'low' | 'moderate' | 'high'
    fallRisk?: number         // 跌倒风险评分
    fallRiskLevel: 'low' | 'moderate' | 'high'
  }
}

// 待办事项
interface PrintableTodo {
  id: number
  patientId?: number
  patientName?: string
  content: string
  dueTime?: string
  priority: 'high' | 'normal' | 'low'
  status: 'pending' | 'completed'
}
```

### Mock数据示例

```typescript
// frontend/src/mocks/handoverPrintData.ts

export const mockPrintableReport: PrintableReport = {
  id: 1,
  departmentId: 1,
  departmentName: '心内科',
  handoverDate: '2026-03-27',
  shift: '白班',
  fromDoctorId: 1,
  fromDoctorName: '张医生',
  toDoctorId: 2,
  toDoctorName: '李医生',
  status: 'PENDING',

  stats: {
    totalPatients: 8,
    admission: 1,
    transferOut: 1,
    discharge: 1,
    transferIn: 0,
    death: 0,
    surgery: 0,
    criticalCount: 2
  },

  patients: [
    {
      id: 1,
      bedNumber: '1床',
      name: '张三',
      age: 65,
      gender: '男',
      diagnosis: '2型糖尿病、高血压3级',
      condition: '主诉头痛、恶心、呕吐、视物模糊1周。入院查体，神志清楚，双侧瞳孔等大等圆，对光反射迟钝。今日血糖波动较大，最高15.2mmol/L。',
      isCritical: true,
      vitals: {
        temperature: 36.5,
        heartRate: 78,
        bloodPressure: '135/85',
        respiratoryRate: 18,
        spo2: 98
      },
      riskScores: {
        mews: 2,
        mewsLevel: 'moderate',
        braden: 16,
        bradenLevel: 'moderate',
        fallRisk: 25,
        fallRiskLevel: 'high'
      }
    },
    // ... 更多患者
  ],

  reportContent: {
    situation: '心内科今日共8名患者，重点患者2名。张三（1床）血糖波动较大，最高15.2mmol/L；李四（2床）胸闷症状加重。',
    background: '张三：入院日期2026-03-15，2型糖尿病病史5年，今日血糖控制不佳。李四：入院日期2026-03-10，冠心病史，今日胸闷加重。',
    assessment: '张三：血糖控制不佳，MEWS评分2分（中风险），需加强监测。李四：病情相对稳定，MEWS评分1分。',
    recommendation: '张三：监测Q4H血糖，胰岛素调整，明日内分泌科会诊。李四：继续监测血压，完善心脏彩超检查。'
  },

  todos: [
    {
      id: 1,
      patientId: 1,
      patientName: '张三',
      content: '明日空腹血糖复查',
      dueTime: '2026-03-28 06:00',
      priority: 'high',
      status: 'pending'
    },
    {
      id: 2,
      patientId: 1,
      patientName: '张三',
      content: '内分泌科会诊',
      dueTime: '2026-03-28 14:00',
      priority: 'high',
      status: 'pending'
    }
  ],

  printMetadata: {
    generatedAt: '2026-03-27T08:30:00Z',
    version: 1,
    isSigned: false
  }
}
```

## 组件设计

### 组件清单

```
frontend/src/components/
└── handover/
    ├── CreateHandoverDialog.vue       # 已存在 - 添加预览按钮
    ├── ReportPreview.vue              # 新增 - 报告预览弹窗
    ├── ReportPrintView.vue            # 新增 - 打印专用视图
    └── VoiceInputDialog.vue           # 已存在

frontend/src/views/
├── HandoverManagement.vue             # 已存在 - 添加打印按钮
└── ...

frontend/src/styles/
├── print.css                          # 新增 - 打印样式
└── ...

frontend/src/mocks/
├── handoverPrintData.ts               # 新增 - 打印Mock数据
└── ...
```

### ReportPreview.vue (预览弹窗组件)

```typescript
// Props
type Props = {
  modelValue: boolean           // 控制弹窗显示
  reportData: PrintableReport   // 报表数据
  mode: 'preview' | 'view'      // 模式: preview=提交前, view=查看详情
}

// Events
type Emits = {
  'update:modelValue': (value: boolean) => void
  print: () => void             // 点击打印
  exportPdf: () => void         // 点击导出PDF
  close: () => void             // 点击关闭
}

// 功能
- 全屏弹窗展示完整报告
- 内部滚动浏览
- 提供打印、导出PDF、关闭按钮
```

### ReportPrintView.vue (打印视图组件)

```typescript
// Props
type Props = {
  reportData: PrintableReport   // 报表数据
  showSignature: boolean        // 是否显示签名栏
}

// 功能
- 纯展示组件，无交互
- 优化打印样式
- 分页控制（避免表格跨页断行）
- 页眉页脚（医院logo、页码）
```

### 打印样式 (print.css)

```css
/* 打印专用样式 */
@media print {
  /* 隐藏非打印元素 */
  .no-print,
  .el-dialog__header,
  .el-dialog__footer,
  .preview-toolbar {
    display: none !important;
  }

  /* 调整弹窗为全页 */
  .el-dialog {
    position: static !important;
    margin: 0 !important;
    width: 100% !important;
    max-width: none !important;
    box-shadow: none !important;
  }

  .el-dialog__body {
    padding: 20px !important;
    max-height: none !important;
    overflow: visible !important;
  }

  /* A4纸张设置 */
  @page {
    size: A4;
    margin: 15mm;
  }

  /* 分页控制 */
  .patient-item {
    page-break-inside: avoid;
  }

  .section {
    page-break-inside: avoid;
  }

  /* 打印优化 */
  body {
    font-size: 12pt;
    line-height: 1.5;
    color: #000;
  }

  /* 确保背景色打印 */
  * {
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }
}

/* 预览模式样式 */
.report-preview-container {
  background: #fff;
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
  box-shadow: 0 0 20px rgba(0,0,0,0.1);
}

/* 医院抬头 */
.report-header {
  text-align: center;
  border-bottom: 2px solid #333;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.report-header h1 {
  font-size: 22px;
  font-weight: bold;
  margin: 0;
}

.report-header h2 {
  font-size: 16px;
  font-weight: normal;
  margin: 10px 0 0;
  color: #666;
}
```

## 技术方案

### 打印实现方式

采用 **浏览器原生打印 (window.print())** 方案:

```
优点:
- 无需后端支持，纯前端实现
- 用户可自定义打印机、纸张、页边距
- 支持打印预览
- 兼容性好

实现:
1. CSS @media print 媒体查询控制打印样式
2. window.print() 触发打印对话框
3. 打印前临时修改DOM样式，打印后恢复
```

### 打印流程代码

```typescript
// 打印函数
const handlePrint = () => {
  // 保存当前样式
  const originalTitle = document.title

  // 设置打印标题
  document.title = `${reportData.departmentName}_${reportData.shift}_交班报告`

  // 触发打印
  window.print()

  // 恢复标题
  document.title = originalTitle
}
```

### 集成点

#### 1. CreateHandoverDialog.vue 修改

```typescript
// 添加预览按钮
const handlePreview = () => {
  // 验证必要字段
  if (!form.toDoctorId) {
    ElMessage.warning('请选择接班医生')
    return
  }

  // 生成预览数据（合并当前表单数据+Mock数据）
  const previewData = generatePreviewData(form)
  currentPreviewData.value = previewData
  showPreviewDialog.value = true
}

// 按钮布局
template: `
  <el-button @click="handleClose" class="cancel-btn">取消</el-button>
  <el-button type="info" plain @click="handlePreview">
    <el-icon><Document /></el-icon>
    预览报告
  </el-button>
  <el-button type="primary" :loading="loading" @click="handleSubmit">
    确认发起交班
  </el-button>
`
```

#### 2. HandoverManagement.vue 修改

```typescript
// 详情页添加打印按钮
const handlePrint = (row: HandoverRecord) => {
  // 加载完整数据（含患者详情、SBAR报告）
  const fullData = loadFullReportData(row.id)
  currentReportData.value = fullData
  showPreviewDialog.value = true
}

// 按钮布局
template: `
  <el-button @click="showViewDialog = false">关闭</el-button>
  <el-button type="info" plain @click="handlePrint(currentHandover)">
    <el-icon><Printer /></el-icon>
    打印报告
  </el-button>
  <el-button type="primary" @click="handleConfirmAccept">
    确认接班
  </el-button>
`
```

## 界面状态

### CreateHandoverDialog 状态变化

```
初始状态:
┌────────────────────────────────────────┐
│ [取消]                      [确认发起] │
└────────────────────────────────────────┘

添加预览后:
┌─────────────────────────────────────────────────────┐
│ [取消]  [预览报告]                      [确认发起] │
└─────────────────────────────────────────────────────┘

预览弹窗状态:
┌──────────────────────────────────────────────────────────────┐
│ 预览弹窗标题栏                                                │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│                    [报告内容预览区]                           │
│                                                              │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│ [返回编辑]  [导出PDF]  [打印]                                │
└──────────────────────────────────────────────────────────────┘
```

### HandoverManagement 详情页状态变化

```
TRANSFERRING 状态 (接班确认前):
┌─────────────────────────────────────────────────────────────────┐
│ [关闭]                                  [打印]  [确认接班]     │
└─────────────────────────────────────────────────────────────────┘

COMPLETED 状态 (已完成):
┌─────────────────────────────────────────────────────────────────┐
│ [关闭]                                   [打印]                 │
└─────────────────────────────────────────────────────────────────┘
```

## 交互说明

| 交互点 | 触发条件 | 行为 |
|--------|----------|------|
| 预览报告按钮 | 在CreateHandoverDialog点击 | 打开ReportPreview弹窗，展示当前表单数据生成的报告预览 |
| 打印按钮 | 在ReportPreview或详情页点击 | 调用window.print()，使用print.css样式 |
| 导出PDF按钮 | 在ReportPreview点击 | (Phase 1暂不实现，预留接口) |
| 返回编辑 | 在ReportPreview点击 | 关闭弹窗，返回CreateHandoverDialog |

## 验收标准

1. ✅ CreateHandoverDialog 新增「预览报告」按钮，点击打开预览弹窗
2. ✅ ReportPreview 组件正确展示交班报告完整内容
3. ✅ 打印功能调用浏览器原生打印，样式正确
4. ✅ HandoverManagement 详情页新增「打印报告」按钮
5. ✅ 打印样式适配A4纸张，包含医院抬头、科室信息、患者列表、SBAR摘要、待办事项、签名栏
6. ✅ 打印时隐藏弹窗按钮、滚动条等非打印元素

## 任务拆分

- [ ] 创建 ReportPreview.vue 预览弹窗组件
- [ ] 创建 ReportPrintView.vue 打印视图子组件
- [ ] 创建 print.css 打印样式文件
- [ ] 创建 handoverPrintData.ts Mock数据
- [ ] 修改 CreateHandoverDialog.vue，添加预览按钮和逻辑
- [ ] 修改 HandoverManagement.vue，详情页添加打印按钮
- [ ] 测试打印效果（Chrome浏览器）
