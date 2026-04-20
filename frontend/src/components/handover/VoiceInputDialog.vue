<template>
  <el-dialog
    v-model="visible"
    title=""
    width="600px"
    :close-on-click-modal="false"
    class="voice-dialog"
    @closed="handleClose"
    @open="handleOpen"
  >
<!-- 模式切换 -->
        <div v-if="!isInlineMode" class="mode-switch">
          <el-radio-group v-model="inputMode" size="small">
        <el-radio-button value="single">单人录入</el-radio-button>
        <el-radio-button value="batch">批量录入</el-radio-button>
      </el-radio-group>
    </div>

    <div class="voice-container">
      <!-- 录音状态 -->
      <div v-if="status === 'recording'" class="recording-section">
        <div class="wave-animation">
          <span v-for="i in 8" :key="i" class="wave-bar" :style="{ animationDelay: `${i * 0.1}s` }"></span>
        </div>
        <div class="recording-hint">
          {{ inputMode === 'batch' ? '正在录音，请连续说出多个患者病情...' : '正在录音，请说出患者病情信息...' }}
        </div>
        <div class="example-text">
          {{ inputMode === 'batch' ? '示例："2床张三，患者今日出院。17床李四，病情稳定"' : '示例："患者今日出院，病情稳定"' }}
        </div>
        
        <!-- 实时转写内容 -->
        <div class="transcript-box">
          <div v-if="transcriptText || interimTranscriptDisplay" class="transcript-text">
            <span class="final-text">{{ transcriptText }}</span>
            <span v-if="interimTranscriptDisplay" class="interim-text">{{ interimTranscriptDisplay }}</span>
          </div>
          <div v-else class="transcript-placeholder">转写内容将实时显示在这里...</div>
        </div>
        
        <div class="action-buttons">
          <el-button @click="handlePause" size="large">
            <el-icon><VideoPause /></el-icon>
            暂停
          </el-button>
          <el-button type="primary" @click="handleComplete" size="large">
            <el-icon><Check /></el-icon>
            完成
          </el-button>
        </div>
      </div>

      <!-- 暂停状态 -->
      <div v-else-if="status === 'paused'" class="paused-section">
        <div class="paused-icon">
          <el-icon :size="48"><VideoPause /></el-icon>
        </div>
        <div class="paused-hint">录音已暂停</div>
        
        <div class="transcript-box">
          <div v-if="transcriptText" class="transcript-text">
            <span class="final-text">{{ transcriptText }}</span>
          </div>
          <div v-else class="transcript-placeholder">暂无转写内容</div>
        </div>
        
        <div class="action-buttons">
          <el-button @click="handleResume" size="large">
            <el-icon><VideoPlay /></el-icon>
            继续
          </el-button>
          <el-button type="primary" @click="handleComplete" size="large">
            <el-icon><Check /></el-icon>
            完成
          </el-button>
          <el-button type="danger" @click="handleCancel" size="large">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </div>
      </div>

      <!-- 无麦克风：显示二维码 -->
      <div v-else-if="status === 'no-mic'" class="qrcode-section">
        <div class="qrcode-title">📱 请使用手机扫码录音</div>
        <div class="qrcode-box">
          <img v-if="qrcodeDataUrl" :src="qrcodeDataUrl" alt="扫码录音二维码" class="qrcode-image" />
          <div v-else class="qrcode-placeholder">
            <el-icon :size="80"><Iphone /></el-icon>
            <div class="qrcode-text">二维码生成中...</div>
          </div>
        </div>
        <div class="session-info">
          <span>会话ID: {{ sessionId }}</span>
          <span>有效期: 5分钟</span>
        </div>
        <div class="status-text">
          <el-icon class="loading-icon"><Loading /></el-icon>
          等待手机连接...
        </div>
        <el-button @click="handleCancelQrcode" type="info" plain>取消</el-button>
      </div>

      <!-- 处理中 -->
      <div v-else-if="status === 'processing'" class="processing-section">
        <el-icon class="processing-icon" :size="48"><Loading /></el-icon>
        <div class="processing-text">正在处理语音内容...</div>
      </div>

      <!-- 编辑界面 (inline模式) -->
      <div v-else-if="status === 'edit'" class="edit-section">
        <div class="edit-title">
          <el-icon><Document /></el-icon>
          语音识别结果
        </div>
        
        <div v-if="patient" class="patient-info">
          <span class="bed-number">{{ patient.bedNumber }}</span>
          <span class="patient-name">{{ patient.name }}</span>
          <el-tag size="small" type="info">{{ patient.diagnosis }}</el-tag>
        </div>

        <div class="edit-content">
          <el-input
            v-model="editableContent"
            type="textarea"
            :rows="6"
            placeholder="请输入或修改患者病情信息..."
            resize="none"
          />
        </div>

        <div class="edit-actions">
          <el-button @click="handleCancelEdit">取消</el-button>
          <el-button type="primary" @click="handleConfirmEdit">
            <el-icon><Check /></el-icon>
            确认应用
          </el-button>
        </div>
      </div>

      <!-- 结果预览 -->
      <div v-else-if="status === 'preview'" class="preview-section">
        <div class="preview-title">
          <el-icon><Document /></el-icon>
          识别结果预览
        </div>
        
        <div class="preview-list">
          <div 
            v-for="(item, index) in parsedResults" 
            :key="index" 
            class="preview-item"
            :class="{ 'has-conflict': item.hasConflict, 'is-new': item.isNew }"
          >
            <div class="item-header">
              <span class="bed-number">{{ item.bedNumber }}</span>
              <span class="patient-name">{{ item.patientName }}</span>
              <el-tag v-if="item.hasConflict" type="danger" size="small">需确认</el-tag>
              <el-tag v-else-if="item.isNew" type="success" size="small">新增</el-tag>
              <el-tag v-else-if="item.matchStatus === 'matched'" type="info" size="small">无变化</el-tag>
            </div>
            <div class="item-content">
              <div v-if="item.hasConflict" class="conflict-content">
                <span class="original">原：{{ item.originalContent }}</span>
                <span class="arrow">→</span>
                <span class="new-content">{{ item.newContent }}</span>
              </div>
              <div v-else-if="item.isNew" class="new-content-text">
                {{ item.mergedContent }}
                <span class="added-mark">（新增）</span>
              </div>
              <div v-else class="normal-content">
                {{ item.mergedContent || '无变化' }}
              </div>
            </div>
            <div v-if="item.errorMessage" class="error-message">
              <el-icon><WarningFilled /></el-icon>
              {{ item.errorMessage }}
            </div>
          </div>
        </div>

        <div class="preview-summary">
          <span v-if="conflictCount > 0" class="conflict-hint">
            <el-icon><WarningFilled /></el-icon>
            {{ conflictCount }} 条内容需要确认
          </span>
          <span v-else class="success-hint">
            <el-icon><CircleCheckFilled /></el-icon>
            全部内容已识别，可直接应用
          </span>
        </div>

        <div class="action-buttons">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleApply">应用结果</el-button>
        </div>
      </div>

      <!-- 批量匹配预览 -->
      <div v-else-if="status === 'batch-preview'" class="batch-preview-section">
        <div class="preview-title">
          <el-icon><Document /></el-icon>
          批量匹配结果
        </div>
        
        <div class="batch-list">
          <div
            v-for="(item, index) in batchMatchItems"
            :key="index"
            class="batch-item"
            :class="{
              'matched': item.matchResult.status === 'matched',
              'not-found': item.matchResult.status === 'not_found',
              'duplicate': item.matchResult.status === 'duplicate_name'
            }"
          >
            <div class="batch-item-header">
              <span class="bed-number">{{ item.parsedItem.bedNumber || '未知床号' }}</span>
              <span class="patient-name">{{ item.parsedItem.patientName || '未知姓名' }}</span>
              <el-tag
                v-if="item.matchResult.status === 'matched'"
                type="success"
                size="small"
              >
                已匹配
              </el-tag>
              <el-tag
                v-else-if="item.matchResult.status === 'not_found'"
                type="danger"
                size="small"
              >
                未匹配
              </el-tag>
              <el-tag
                v-else-if="item.matchResult.status === 'duplicate_name'"
                type="warning"
                size="small"
              >
                重名
              </el-tag>
            </div>
            <div class="batch-item-content">{{ item.parsedItem.content }}</div>
            
            <!-- 患者选择 -->
            <div v-if="item.matchResult.status !== 'matched'" class="patient-select">
              <el-select
                v-model="item.selectedPatientId"
                placeholder="选择患者"
                size="small"
                style="width: 200px"
              >
                <el-option
                  v-for="patient in (item.matchResult.candidates || props.patients)"
                  :key="patient.id"
                  :label="`${patient.bedNumber} ${patient.name}`"
                  :value="patient.id"
                />
              </el-select>
            </div>
            
            <div v-if="item.matchResult.message" class="error-message">
              <el-icon><WarningFilled /></el-icon>
              {{ item.matchResult.message }}
            </div>
          </div>
        </div>

        <div class="batch-summary">
          <span class="matched-count">
            已匹配: {{ batchMatchItems.filter(i => i.matchResult.status === 'matched' || i.selectedPatientId).length }}条
          </span>
          <span class="total-count">
            共: {{ batchMatchItems.length }}条
          </span>
        </div>

        <div class="action-buttons">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleBatchApply">批量应用</el-button>
        </div>
      </div>
    </div>

    <!-- 底部提示（录音状态时显示） -->
    <template #footer>
      <div v-if="status === 'recording'" class="footer-tip">
        💡 没有麦克风？
        <el-button type="primary" link @click="switchToQrcode">显示二维码</el-button>
        用手机录音
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoPause,
  Check,
  Iphone,
  Loading,
  Document,
  WarningFilled,
  CircleCheckFilled,
  VideoPlay,
  Close,
} from '@element-plus/icons-vue'
import { parseVoiceText, type ParsedVoiceItem } from '@/utils/voiceParser'
import { batchMatchPatients, type MatchResult } from '@/utils/patientMatcher'
import type { PatientInfo } from '@/types/patient'
import { checkVoiceSessionStatus, createVoiceSession } from '@/api/voice'
import { VoiceWebSocket } from '@/utils/voiceWebSocket'
import QRCode from 'qrcode'

interface Props {
  modelValue: boolean
  patients: PatientInfo[]
  patient?: PatientInfo
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'apply', results: ParsedResult[]): void
  (e: 'batchApply', items: BatchMatchItem[]): void
  (e: 'inlineApply', content: string): void
}

interface ParsedResult {
  bedNumber: string
  patientName: string
  patientId?: number
  originalContent: string
  newContent: string
  mergedContent: string
  matchStatus: 'matched' | 'not_found' | 'duplicate_name'
  hasConflict: boolean
  isNew: boolean
  errorMessage?: string
}

interface BatchMatchItem {
  parsedItem: ParsedVoiceItem
  matchResult: MatchResult
  selectedPatientId?: number
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const inputMode = ref<'single' | 'batch'>('single')
const status = ref<'recording' | 'paused' | 'no-mic' | 'processing' | 'preview' | 'batch-preview' | 'edit'>('recording')
const sessionId = ref('')
const transcriptText = ref('')
const interimTranscriptDisplay = ref('')
const parsedResults = ref<ParsedResult[]>([])
const batchMatchItems = ref<BatchMatchItem[]>([])
const qrcodeDataUrl = ref('')
const editableContent = ref('')

const isInlineMode = computed(() => !!props.patient)

let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []
let voiceWebSocket: VoiceWebSocket | null = null
let pollingTimer: number | null = null

// 冲突关键词对
const conflictKeywords: Record<string, string[]> = {
  '出院': ['入院', '继续治疗', '观察', '手术'],
  '入院': ['出院'],
  '好转': ['加重', '恶化'],
  '加重': ['好转', '稳定'],
  '稳定': ['危急', '病危', '恶化'],
  '手术': ['保守治疗'],
  '死亡': ['好转', '稳定', '出院']
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetState()
  }
})

const resetState = () => {
  status.value = 'recording'
  transcriptText.value = ''
  interimTranscriptDisplay.value = ''
  parsedResults.value = []
  batchMatchItems.value = []
  audioChunks = []
  qrcodeDataUrl.value = ''
  stopPolling()
}

const handleOpen = async () => {
  const hasMic = await checkMicrophone()
  if (hasMic) {
    status.value = 'recording'
    startRecording()
  } else {
    status.value = 'no-mic'
    generateSessionId()
  }
}

const handleClose = () => {
  stopRecording()
  stopTranscription()
  visible.value = false
}

const checkMicrophone = async (): Promise<boolean> => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    stream.getTracks().forEach(track => track.stop())
    return true
  } catch {
    return false
  }
}

const startRecording = async () => {
  await startSpeechRecognition()
}

const startSpeechRecognition = async () => {
  const hostname = window.location.hostname === 'localhost' ? 'localhost' : window.location.hostname
  const port = '8080'
  const wsUrl = `ws://${hostname}:${port}/ws/voice`
  
  voiceWebSocket = new VoiceWebSocket(wsUrl, {
    onConnected: () => {
      voiceWebSocket?.startRecording()
    },
    onPartial: (text) => {
      interimTranscriptDisplay.value = text
    },
    onResult: (text) => {
      transcriptText.value += text
      interimTranscriptDisplay.value = ''
    },
    onFinal: (text) => {
      if (text) {
        transcriptText.value += text
      }
      interimTranscriptDisplay.value = ''
    },
    onError: (error) => {
      ElMessage.error(`语音识别错误: ${error}`)
    },
    onDisconnected: () => {
      if (status.value === 'recording') {
        ElMessage.warning('语音识别连接断开')
      }
    },
    maxRetries: 3,
    retryDelay: 1000
  })
  
  try {
    await voiceWebSocket.connect()
  } catch (error) {
    ElMessage.error('启动语音识别失败')
    console.error('WebSocket connection error:', error)
    status.value = 'no-mic'
    generateSessionId()
  }
}

const stopRecording = () => {
  if (voiceWebSocket) {
    voiceWebSocket.stopRecording()
  }
}

const stopTranscription = () => {
  if (voiceWebSocket) {
    voiceWebSocket.disconnect()
    voiceWebSocket = null
  }
}

const handlePause = () => {
  stopRecording()
  stopTranscription()
  status.value = 'paused'
}

const handleResume = async () => {
  status.value = 'recording'
  await startSpeechRecognition()
}

const handleStop = async () => {
  stopTranscription()
  transcriptText.value = ''
  interimTranscriptDisplay.value = ''
  await startSpeechRecognition()
}

const handleComplete = () => {
  const savedTranscript = transcriptText.value
  
  if (voiceWebSocket) {
    voiceWebSocket.sendEnd()
    voiceWebSocket.stopRecording()
  }
  stopTranscription()

  if (isInlineMode.value) {
    editableContent.value = savedTranscript
    status.value = 'edit'
    return
  }

  status.value = 'processing'

  setTimeout(() => {
    if (inputMode.value === 'batch') {
      processBatchMode()
    } else {
      parseAndMerge(transcriptText.value)
      status.value = 'preview'
    }
  }, 1000)
}

const handleConfirmEdit = () => {
  emit('inlineApply', editableContent.value)
  ElMessage.success('语音内容已应用')
  visible.value = false
}

const handleCancelEdit = () => {
  visible.value = false
}

const processBatchMode = () => {
  const parseResult = parseVoiceText(transcriptText.value)
  const matchedItems = batchMatchPatients(parseResult.items, props.patients)

  batchMatchItems.value = matchedItems.map(({ item, match }) => ({
    parsedItem: item,
    matchResult: match,
    selectedPatientId: match.status === 'matched' ? match.patientId : undefined,
  }))

  status.value = 'batch-preview'
}

const generateQrcode = async () => {
  try {
    const res = await createVoiceSession()
    if (res.code === 0 && res.data) {
      sessionId.value = res.data.sessionId
    } else {
      sessionId.value = 'VC' + Date.now().toString(36).toUpperCase()
    }
  } catch {
    sessionId.value = 'VC' + Date.now().toString(36).toUpperCase()
  }

  const protocol = window.location.protocol
  const hostname = window.location.hostname === 'localhost' 
    ? '10.193.142.39' 
    : window.location.hostname
  const port = window.location.port || '3000'
  const baseUrl = `${protocol}//${hostname}:${port}`
  const mobileUrl = `${baseUrl}/mobile-voice?session=${sessionId.value}`

  try {
    qrcodeDataUrl.value = await QRCode.toDataURL(mobileUrl, {
      width: 200,
      margin: 2,
      color: {
        dark: '#333333',
        light: '#ffffff'
      }
    })
  } catch (error) {
    console.error('Failed to generate QR code:', error)
    qrcodeDataUrl.value = ''
  }
}

const switchToQrcode = async () => {
  stopRecording()
  stopTranscription()
  status.value = 'no-mic'
  await generateQrcode()
  startPolling()
}

const startPolling = () => {
  stopPolling()
  pollingTimer = window.setInterval(async () => {
    if (status.value !== 'no-mic' || !sessionId.value) {
      stopPolling()
      return
    }

    try {
      const res = await checkVoiceSessionStatus(sessionId.value)
      if (res.code === 0 && res.data.hasContent) {
        stopPolling()
        transcriptText.value = res.data.transcript
        status.value = 'processing'

        setTimeout(() => {
          if (inputMode.value === 'batch') {
            processBatchMode()
          } else {
            parseAndMerge(transcriptText.value)
            status.value = 'preview'
          }
        }, 1000)

        ElMessage.success('已接收手机录音内容')
      }
    } catch (error) {
      console.error('Polling error:', error)
    }
  }, 3000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

const handleCancelQrcode = () => {
  stopPolling()
  visible.value = false
}

const handleCancel = () => {
  stopRecording()
  stopTranscription()
  stopPolling()
  visible.value = false
}

// 解析文本并合并
const parseAndMerge = (text: string) => {
  const results: ParsedResult[] = []
  
  // 按句号分割
  const sentences = text.split(/[。；;]/g).filter(s => s.trim())
  
  for (const sentence of sentences) {
    const result = parseSentence(sentence.trim())
    if (result) {
      results.push(result)
    }
  }
  
  parsedResults.value = results
}

// 解析单个句子
const parseSentence = (sentence: string): ParsedResult | null => {
  // 正则匹配：床号 + 姓名 + 病情
  // 支持格式：2床张三，病情... / 第2床张三，病情... / 张三，病情...
  const bedNamePattern = /(?:第)?(\d+)床\s*(\S+?)\s*[，,]\s*(.+)/
  const nameOnlyPattern = /^(\S+?)\s*[，,]\s*(.+)/
  
  let bedNumber = ''
  let patientName = ''
  let newContent = ''
  
  // 尝试匹配床号+姓名
  const bedNameMatch = sentence.match(bedNamePattern)
  if (bedNameMatch) {
    bedNumber = bedNameMatch[1] + '床'
    patientName = bedNameMatch[2]
    newContent = bedNameMatch[3]
  } else {
    // 尝试仅匹配姓名
    const nameMatch = sentence.match(nameOnlyPattern)
    if (nameMatch) {
      patientName = nameMatch[1]
      newContent = nameMatch[2]
    } else {
      return null
    }
  }
  
  // 匹配患者
  const matchResult = matchPatient(bedNumber, patientName)
  
  if (matchResult.status === 'not_found') {
    return {
      bedNumber,
      patientName,
      matchStatus: 'not_found',
      originalContent: '',
      newContent,
      mergedContent: '',
      hasConflict: false,
      isNew: false,
      errorMessage: '未找到匹配的患者'
    }
  }
  
  if (matchResult.status === 'duplicate_name') {
    return {
      bedNumber,
      patientName,
      matchStatus: 'duplicate_name',
      originalContent: '',
      newContent,
      mergedContent: '',
      hasConflict: false,
      isNew: false,
      errorMessage: '存在重名患者，请指定床号'
    }
  }
  
  const patient = matchResult.patient!
  const originalContent = patient.condition || ''
  
  // 内容比对
  const mergeResult = mergeContent(originalContent, newContent)
  
  return {
    bedNumber: patient.bedNumber,
    patientName: patient.name,
    patientId: patient.id,
    originalContent,
    newContent,
    mergedContent: mergeResult.content,
    matchStatus: 'matched',
    hasConflict: mergeResult.hasConflict,
    isNew: mergeResult.isNew
  }
}

// 匹配患者
const matchPatient = (bedNumber: string, patientName: string): { 
  status: 'matched' | 'not_found' | 'duplicate_name'
  patient?: PatientInfo 
} => {
  let candidates = [...props.patients]
  
  // 按床号筛选
  if (bedNumber) {
    candidates = candidates.filter(p => p.bedNumber === bedNumber)
  }
  
  // 按姓名筛选
  if (patientName) {
    candidates = candidates.filter(p => p.name === patientName)
  }
  
  if (candidates.length === 0) {
    return { status: 'not_found' }
  }
  
  if (candidates.length === 1) {
    return { status: 'matched', patient: candidates[0] }
  }
  
  // 多个匹配，说明仅通过姓名匹配且重名
  if (!bedNumber && candidates.length > 1) {
    return { status: 'duplicate_name' }
  }
  
  return { status: 'matched', patient: candidates[0] }
}

// 内容合并
const mergeContent = (original: string, newInfo: string): { 
  content: string
  hasConflict: boolean
  isNew: boolean 
} => {
  // 相同内容
  if (original === newInfo || !newInfo) {
    return { content: original, hasConflict: false, isNew: false }
  }
  
  // 原内容为空
  if (!original) {
    return { content: newInfo, hasConflict: false, isNew: true }
  }
  
  // 检查冲突
  if (checkConflict(original, newInfo)) {
    return { content: `${original} → ${newInfo}`, hasConflict: true, isNew: false }
  }
  
  // 检查是否包含
  if (original.includes(newInfo) || newInfo.includes(original)) {
    return { content: original, hasConflict: false, isNew: false }
  }
  
  // 追加
  return { content: `${original}。${newInfo}`, hasConflict: false, isNew: true }
}

// 检查冲突
const checkConflict = (original: string, newInfo: string): boolean => {
  for (const [keyword, conflicts] of Object.entries(conflictKeywords)) {
    if (newInfo.includes(keyword)) {
      for (const conflict of conflicts) {
        if (original.includes(conflict)) {
          return true
        }
      }
    }
    if (original.includes(keyword)) {
      for (const conflict of conflicts) {
        if (newInfo.includes(conflict)) {
          return true
        }
      }
    }
  }
  return false
}

// 冲突数量
const conflictCount = computed(() => {
  return parsedResults.value.filter(r => r.hasConflict).length
})

const handleApply = () => {
  if (isInlineMode.value) {
    emit('inlineApply', transcriptText.value)
    visible.value = false
    return
  }

  const validResults = parsedResults.value.filter(
    r => r.matchStatus === 'matched' && r.mergedContent
  )

  emit('apply', validResults)
  ElMessage.success('语音内容已应用')
  visible.value = false
}

const handleBatchApply = () => {
  const validItems = batchMatchItems.value.filter(
    (item) => item.matchResult.status === 'matched' || item.selectedPatientId
  )

  if (validItems.length === 0) {
    ElMessage.warning('没有可应用的内容')
    return
  }

  emit('batchApply', validItems)
  ElMessage.success(`已应用 ${validItems.length} 条患者病情`)
  visible.value = false
}

onUnmounted(() => {
  stopRecording()
  stopTranscription()
  stopPolling()
})
</script>

<style scoped>
.mode-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.voice-dialog :deep(.el-dialog__header) {
  display: none;
}

.voice-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.voice-container {
  min-height: 300px;
}

/* 录音状态 */
.recording-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.wave-animation {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 60px;
}

.wave-bar {
  width: 6px;
  height: 20px;
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  border-radius: 3px;
  animation: wave 0.8s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% { height: 20px; }
  50% { height: 50px; }
}

.recording-hint {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.example-text {
  font-size: 13px;
  color: var(--text-secondary);
}

.transcript-box {
  width: 100%;
  min-height: 80px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.transcript-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.final-text {
  color: var(--text-primary);
}

.interim-text {
  color: var(--text-secondary);
  opacity: 0.7;
}

.transcript-placeholder {
  font-size: 14px;
  color: var(--text-placeholder);
}

.action-buttons {
  display: flex;
  gap: 16px;
}

/* 二维码状态 */
.qrcode-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.qrcode-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.qrcode-box {
  width: 200px;
  height: 200px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}

.qrcode-image {
  width: 180px;
  height: 180px;
}

.qrcode-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

.qrcode-text {
  font-size: 13px;
}

.session-info {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
}

.status-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--color-primary-DEFAULT);
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 处理中 */
.processing-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 16px;
}

.processing-icon {
  color: var(--color-primary-DEFAULT);
  animation: spin 1s linear infinite;
}

.processing-text {
  font-size: 16px;
  color: var(--text-secondary);
}

/* 预览状态 */
.preview-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.preview-title .el-icon {
  color: var(--color-primary-DEFAULT);
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.preview-item {
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  border-left: 4px solid var(--border-color);
}

.preview-item.has-conflict {
  border-left-color: var(--color-danger);
  background: #fff1f0;
}

.preview-item.is-new {
  border-left-color: var(--color-success);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.bed-number {
  font-weight: 600;
  color: var(--color-primary-DEFAULT);
}

.patient-name {
  font-weight: 500;
  color: var(--text-primary);
}

.item-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.conflict-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.conflict-content .original {
  color: var(--text-secondary);
  text-decoration: line-through;
}

.conflict-content .arrow {
  color: var(--color-danger);
}

.conflict-content .new-content {
  color: var(--color-danger);
  font-weight: 500;
}

.added-mark {
  color: var(--color-success);
  font-size: 12px;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--color-danger);
}

.preview-summary {
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
}

.conflict-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-danger);
  font-size: 14px;
}

.success-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-success);
  font-size: 14px;
}

.footer-tip {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

/* 编辑状态 */
.edit-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.edit-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.edit-title .el-icon {
  color: var(--color-primary-DEFAULT);
}

.edit-section .patient-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-content {
  width: 100%;
}

.edit-content :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>