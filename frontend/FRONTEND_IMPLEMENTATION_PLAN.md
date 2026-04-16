# 医护智能交接班系统 - 前端实现方案

## 🎨 设计风格

### 配色方案
- **主色调**：淡橙色 `#FFB366` / `#FF9443`
- **背景色**：白色 `#FFFFFF` + 浅灰 `#F5F7FA`
- **辅助色**：
  - 成功：`#67C23A`
  - 警告：`#E6A23C`
  - 危险：`#F56C6C`
  - 信息：`#409EFF`

### 视觉效果
- **磨砂玻璃**：`backdrop-filter: blur(10px)`
- **圆角**：`border-radius: 12px`（统一圆角风格）
- **阴影**：柔和阴影 `box-shadow: 0 2px 12px rgba(0,0,0,0.1)`
- **过渡**：`transition: all 0.3s ease`

---

## 📁 项目结构

```
frontend/
├── src/
│   ├── assets/              # 静态资源
│   │   ├── images/          # 图片资源
│   │   └── icons/           # SVG 图标
│   ├── components/          # 公共组件
│   │   ├── common/          # 通用组件
│   │   │   ├── PageHeader.vue       # 页面标题
│   │   │   ├── BaseTable.vue        # 表格封装
│   │   │   ├── EmptyState.vue       # 空状态
│   │   │   ├── Loading.vue          # 加载组件
│   │   │   └── ConfirmDialog.vue    # 确认对话框
│   │   ├── layout/          # 布局组件
│   │   │   ├── DefaultLayout.vue    # 默认布局
│   │   │   ├── TopNav.vue           # 顶部导航
│   │   │   └── SidebarMenu.vue      # 侧边栏
│   │   ├── patient/         # 患者相关组件
│   │   │   ├── PatientList.vue      # 患者列表
│   │   │   ├── PatientCard.vue      # 患者卡片
│   │   │   ├── PatientDialog.vue    # 患者编辑弹窗
│   │   │   ├── VitalsForm.vue       # 生命体征表单
│   │   │   └── VitalsTimeline.vue   # 生命体征时间轴
│   │   ├── risk/            # 风险评估组件
│   │   │   ├── MewsForm.vue         # MEWS 评分
│   │   │   ├── BradenForm.vue       # Braden 评分
│   │   │   ├── FallRiskForm.vue     # 跌倒风险
│   │   │   └── RiskOverview.vue     # 风险总览
│   │   ├── report/          # 报告相关组件
│   │   │   ├── ReportEditor.vue     # 报告编辑器
│   │   │   ├── ReportPreview.vue    # 报告预览
│   │   │   ├── SbarSection.vue      # SBAR 分段
│   │   │   └── VoiceInputButton.vue # 语音录入按钮
│   │   ├── handover/        # 交接班组件
│   │   │   ├── HandoverList.vue     # 交班列表
│   │   │   ├── HandoverCreate.vue   # 发起交班
│   │   │   ├── HandoverAccept.vue   # 接班确认
│   │   │   └── StatusTag.vue        # 状态标签
│   │   └── todo/            # 待办事项组件
│   │       ├── TodoList.vue         # 待办列表
│   │       └── TodoCard.vue         # 待办卡片
│   ├── views/               # 页面视图
│   │   ├── Login.vue                # 登录页
│   │   ├── Dashboard.vue            # 仪表盘
│   │   ├── PatientManagement.vue    # 患者管理
│   │   ├── HandoverManagement.vue   # 交班管理
│   │   ├── ReportEditorPage.vue     # 报告编辑页
│   │   └── Settings.vue             # 设置页
│   ├── stores/              # Pinia 状态管理
│   │   ├── auth.ts                  # 认证状态
│   │   ├── patient.ts               # 患者数据
│   │   ├── handover.ts              # 交班数据
│   │   └── todo.ts                  # 待办事项
│   ├── api/                 # API 请求层
│   │   ├── request.ts               # Axios 封装
│   │   ├── auth.ts                  # 认证 API
│   │   ├── patient.ts               # 患者 API
│   │   ├── handover.ts              # 交班 API
│   │   └── report.ts                # 报告 API
│   ├── services/            # 业务服务
│   │   ├── xfyunRtasr.ts            # 讯飞语音转写
│   │   └── dashscope.ts             # 阿里大模型
│   ├── utils/               # 工具函数
│   │   ├── validators.ts            # 表单验证
│   │   ├── formatters.ts            # 数据格式化
│   │   └── constants.ts             # 常量定义
│   ├── styles/              # 样式文件
│   │   ├── theme.css                # 主题样式（已创建）
│   │   └── variables.scss           # SCSS 变量
│   ├── router/              # 路由配置
│   │   └── index.ts
│   ├── App.vue
│   └── main.ts
├── public/
│   └── logo.png
├── package.json
├── vite.config.ts
├── tailwind.config.js
└── tsconfig.json
```

---

## 🔑 关键实现

### 1. 认证模块

**登录页面** (`views/Login.vue`):
```vue
<template>
  <div class="login-container">
    <div class="login-card glass">
      <h1>医护智能交接班系统</h1>
      <el-form :model="loginForm" :rules="rules">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-input">
            <el-input v-model="loginForm.captcha" placeholder="验证码" />
            <img :src="captchaImage" @click="refreshCaptcha" class="captcha-img" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-primary-gradient" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
```

**认证 Store** (`stores/auth.ts`):
```typescript
import { defineStore } from 'pinia'
import { loginApi, getCaptchaApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserInfo | null,
  }),
  
  actions: {
    async login(username: string, password: string, captcha: string) {
      const { data } = await loginApi({ username, password, captcha })
      this.token = data.token
      localStorage.setItem('token', data.token)
      await this.fetchUserInfo()
    },
    
    async fetchUserInfo() {
      // 获取用户信息
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
```

---

### 2. 讯飞语音集成

**讯飞 RTASR 服务** (`services/xfyunRtasr.ts`):
```typescript
import { getRtasrTokenApi } from '@/api/voice'

const APP_ID = '8bc8b3ca'
const API_URL = 'wss://rtasr.xfyun.cn/v1/ws'

export class XfyunRtasrService {
  private ws: WebSocket | null = null
  private audioContext: AudioContext | null = null
  private mediaStream: MediaStream | null = null
  private onResult: ((text: string) => void) | null = null

  async initialize() {
    // 从后端获取临时 token（避免暴露 appsecret）
    const { data } = await getRtasrTokenApi()
    this.token = data.token
    this.ts = data.ts
  }

  async startRecording(onResult: (text: string) => void) {
    await this.initialize()
    this.onResult = onResult

    // 创建 WebSocket 连接
    const sign = this.calculateSign()
    this.ws = new WebSocket(`${API_URL}?appid=${APP_ID}&ts=${this.ts}&sign=${sign}`)
    
    this.ws.onmessage = (event) => {
      const result = JSON.parse(event.data)
      if (result.action === 'started') {
        // 开始录音
        this.startAudioCapture()
      } else if (result.action === 'result') {
        // 处理转写结果
        const text = this.parseResult(result.data)
        this.onResult?.(text)
      }
    }
  }

  private async startAudioCapture() {
    this.mediaStream = await navigator.mediaDevices.getUserMedia({ 
      audio: { 
        sampleRate: 16000,
        channelCount: 1,
        sampleSize: 16
      } 
    })
    
    const audioContext = new AudioContext({ sampleRate: 16000 })
    const source = audioContext.createMediaStreamSource(this.mediaStream)
    const processor = audioContext.createScriptProcessor(4096, 1, 1)
    
    processor.onaudioprocess = (event) => {
      const inputData = event.inputBuffer.getChannelData(0)
      const pcmData = this.floatTo16BitPCM(inputData)
      this.ws?.send(pcmData)
    }
    
    source.connect(processor)
    processor.connect(audioContext.destination)
  }

  stopRecording() {
    this.ws?.close()
    this.mediaStream?.getTracks().forEach(track => track.stop())
    this.audioContext?.close()
  }

  private calculateSign(): string {
    // 计算签名（实际应该在后端计算）
    const signBase = `apiKey=${API_KEY}&ts=${this.ts}`
    return CryptoJS.SHA1(signBase + API_SECRET).toString()
  }

  private parseResult(data: any): string {
    // 解析讯飞返回的转写结果
    return data.cn?.st?.rt?.map((item: any) => 
      item.ws.map((ws: any) => ws.cw.map((cw: any) => cw.w).join('')).join('')
    ).join('') || ''
  }
}

export const xfyunRtasr = new XfyunRtasrService()
```

**语音录入按钮组件** (`components/report/VoiceInputButton.vue`):
```vue
<template>
  <el-button 
    :type="isRecording ? 'danger' : 'primary'"
    :icon="isRecording ? VideoPause : VideoCamera"
    :class="{ 'voice-recording': isRecording }"
    @click="toggleRecording"
  >
    {{ isRecording ? '停止录音' : '语音录入' }}
  </el-button>
  
  <div v-if="isRecording" class="voice-status">
    <span class="recording-indicator"></span>
    正在录音... {{ recordingTime }}s
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { xfyunRtasr } from '@/services/xfyunRtasr'

const emit = defineEmits<{
  (e: 'result', text: string): void
}>()

const isRecording = ref(false)
const recordingTime = ref(0)

const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    await startRecording()
  }
}

const startRecording = async () => {
  await xfyunRtasr.startRecording((text) => {
    emit('result', text)
  })
  isRecording.value = true
  recordingTime.value = 0
}

const stopRecording = () => {
  xfyunRtasr.stopRecording()
  isRecording.value = false
}
</script>

<style scoped>
.voice-recording {
  animation: pulse-orange 1.5s ease-in-out infinite;
}

.voice-status {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-primary-light);
}

.recording-indicator {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: var(--color-danger);
  border-radius: 50%;
  margin-right: 8px;
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
```

---

### 3. 阿里大模型集成

**DashScope 服务** (`services/dashscope.ts`):
```typescript
import axios from 'axios'

const API_BASE_URL = 'https://dashscope.aliyuncs.com/compatible-mode/v1'
const API_KEY = 'sk-sp-758abbba5db646c79da0572e8e694b5e'

export interface ReportGenerationParams {
  patientInfo: string
  vitals24h: string
  labResults: string
  treatments: string
  previousHandover?: string
}

export async function generateReport(params: ReportGenerationParams) {
  const prompt = buildSbarPrompt(params)
  
  const response = await axios.post(
    `${API_BASE_URL}/chat/completions`,
    {
      model: 'qwen-plus',
      messages: [
        {
          role: 'system',
          content: `你是一名经验丰富的临床医生，正在撰写医护交接班报告。
请根据提供的患者数据，生成一份符合 SBAR 规范的交班报告。
要求：
1. 使用规范医学术语
2. 避免模糊表述
3. 危急值必须高亮标记
4. 不确定的内容标注"待确认"`
        },
        {
          role: 'user',
          content: prompt
        }
      ],
      temperature: 0.3,
      max_tokens: 2000,
      stream: true
    },
    {
      headers: {
        'Authorization': `Bearer ${API_KEY}`,
        'Content-Type': 'application/json'
      }
    }
  )
  
  return response.data
}

function buildSbarPrompt(params: ReportGenerationParams): string {
  return `请为以下患者生成交班报告：

患者信息：${params.patientInfo}

24 小时生命体征：
${params.vitals24h}

检验检查异常值：
${params.labResults}

今日治疗：
${params.treatments}

${params.previousHandover ? '上次交班记录：\n' + params.previousHandover : ''}

请按照 SBAR 格式生成报告：
- S (Situation): 患者基本信息、诊断、过敏史
- B (Background): 入院日期、近期变化、生命体征趋势、异常检验结果
- A (Assessment): 当前状况、严重程度、临床轨迹、风险评分
- R (Recommendation): 立即行动、待办事项、应急预案`
}
```

**报告编辑页面** (`views/ReportEditorPage.vue`):
```vue
<template>
  <div class="report-editor-page">
    <page-header title="交班报告编辑">
      <template #actions>
        <voice-input-button @result="insertVoiceText" />
        <el-button type="primary" @click="handleGenerate">
          <el-icon><MagicStick /></el-icon>
          AI 生成
        </el-button>
        <el-button @click="handlePreview">预览</el-button>
        <el-button type="success" @click="handleSave">保存</el-button>
      </template>
    </page-header>

    <div class="editor-container">
      <!-- 左侧：患者信息 -->
      <div class="patient-info-panel">
        <patient-card :patient="patient" />
        <risk-overview :patient-id="patient.id" />
      </div>

      <!-- 右侧：报告编辑 -->
      <div class="editor-panel">
        <sbar-section
          v-for="section in sbarSections"
          :key="section.key"
          :title="section.title"
          :content="reportData[section.key]"
          @update="updateSection"
        />
      </div>
    </div>

    <!-- AI 生成进度 -->
    <generation-progress 
      v-if="isGenerating" 
      :progress="generationProgress" 
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { generateReport } from '@/services/dashscope'
import { xfyunRtasr } from '@/services/xfyunRtasr'

const patient = ref({ /* 患者数据 */ })
const reportData = reactive({
  situation: '',
  background: '',
  assessment: '',
  recommendation: ''
})

const isGenerating = ref(false)
const generationProgress = ref(0)

const sbarSections = [
  { key: 'situation', title: 'S - 现状' },
  { key: 'background', title: 'B - 背景' },
  { key: 'assessment', title: 'A - 评估' },
  { key: 'recommendation', title: 'R - 建议' }
]

const handleGenerate = async () => {
  isGenerating.value = true
  generationProgress.value = 0
  
  try {
    const result = await generateReport({
      patientInfo: formatPatientInfo(patient.value),
      vitals24h: formatVitals(patient.value.vitals),
      labResults: formatLabResults(patient.value.labResults),
      treatments: formatTreatments(patient.value.treatments)
    })
    
    // 解析 AI 生成的 SBAR 报告
    parseSbarReport(result, reportData)
  } catch (error) {
    ElMessage.error('报告生成失败')
  } finally {
    isGenerating.value = false
  }
}

const insertVoiceText = (text: string) => {
  // 将语音转写结果插入到当前编辑位置
  reportData.situation += text
}
</script>

<style scoped>
.report-editor-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.editor-container {
  flex: 1;
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
  padding: 20px;
  overflow: hidden;
}

.patient-info-panel {
  overflow-y: auto;
}

.editor-panel {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 24px;
  overflow-y: auto;
}
</style>
```

---

### 4. Mock 数据服务

**Mock 患者数据** (`mock/patientData.ts`):
```typescript
import { MockMethod } from 'vite-plugin-mock'

const patients = Array.from({ length: 50 }, (_, i) => ({
  id: i + 1,
  bedNumber: `${i + 1}床`,
  name: `患者${i + 1}`,
  age: 40 + Math.floor(Math.random() * 40),
  gender: Math.random() > 0.5 ? '男' : '女',
  diagnosis: ['2 型糖尿病', '高血压', '冠心病', 'COPD'][Math.floor(Math.random() * 4)],
  allergies: Math.random() > 0.7 ? ['青霉素'] : [],
  admissionDate: '2026-03-' + (10 + Math.floor(Math.random() * 10)),
  department: '心内科',
  mewsScore: Math.floor(Math.random() * 5),
  bradenScore: 15 + Math.floor(Math.random() * 8),
  fallRisk: Math.floor(Math.random() * 50)
}))

export default [
  {
    url: '/api/patients',
    method: 'get',
    response: () => {
      return {
        code: 0,
        data: {
          list: patients,
          total: patients.length
        }
      }
    }
  },
  {
    url: '/api/patients/:id',
    method: 'get',
    response: (req) => {
      const patient = patients.find(p => p.id === parseInt(req.params.id))
      return {
        code: 0,
        data: patient
      }
    }
  }
] as MockMethod[]
```

---

## 🚀 开发步骤

### 第 1 步：安装依赖
```bash
cd frontend
npm install --registry https://registry.npmmirror.com
```

### 第 2 步：安装额外依赖
```bash
npm install element-plus @element-plus/icons-vue axios dayjs pinia vue-router
npm install -D tailwindcss postcss autoprefixer sass vite-plugin-mock
```

### 第 3 步：初始化 Tailwind
```bash
npx tailwindcss init -p
```

### 第 4 步：创建配置文件
- `tailwind.config.js`
- `postcss.config.js`

### 第 5 步：创建核心文件
按照上面的项目结构创建所有文件

### 第 6 步：启动开发服务器
```bash
npm run dev
```

---

## 📝 注意事项

1. **讯飞语音**：appsecret 不能暴露在前端，需要后端代理鉴权
2. **阿里大模型**：token 可以放在前端，但建议通过后端转发
3. **Mock 数据**：开发环境使用 Mock，生产环境切换到真实 API
4. **主题定制**：Element Plus 主题变量需要在 `main.ts` 中配置

---

## ✅ 下一步

您确认后，我将：
1. 创建所有组件和页面文件
2. 配置完整的主题和样式
3. 实现 Mock 数据服务
4. 集成讯飞语音和阿里大模型

或者您想先从某个具体模块开始？
