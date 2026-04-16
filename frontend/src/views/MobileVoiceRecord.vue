<template>
  <div class="mobile-voice-page">
    <div class="header">
      <h1>语音录入</h1>
      <div class="session-badge">会话: {{ sessionId }}</div>
    </div>

    <div v-if="status === 'ready'" class="card">
      <div class="mic-icon">🎤</div>
      <div class="title">点击开始录音</div>
      <div class="hint">录音完成后自动上传识别</div>
      
      <button class="btn-primary" @click="startRecord">🎙️ 开始录音</button>
      
      <div class="upload-area">
        <input type="file" accept="audio/*" ref="fileInput" @change="handleUpload" hidden />
        <button class="btn-secondary" @click="triggerUpload">📁 上传已有音频</button>
      </div>
    </div>

    <div v-else-if="status === 'recording'" class="card">
      <div class="wave">
        <span v-for="i in 5" :key="i"></span>
      </div>
      <div class="timer">{{ duration }}s</div>
      <div class="hint-red">正在录音，点击停止</div>
      <button class="btn-danger" @click="stopRecord">⏹ 停止录音</button>
    </div>

    <div v-else-if="status === 'uploading'" class="card">
      <div class="spinner"></div>
      <div class="hint">正在上传识别...</div>
      <div class="progress">{{ progress }}%</div>
    </div>

    <div v-else-if="status === 'result'" class="card">
      <div class="success">✓</div>
      <div class="title">识别完成</div>
      
      <div class="transcript-area">
        <textarea v-model="transcript" placeholder="识别结果（可手动修改）" rows="4"></textarea>
      </div>
      
      <div class="actions">
        <button class="btn-secondary" @click="reset">重新录音</button>
        <button class="btn-primary" @click="submit">提交到电脑</button>
      </div>
    </div>

    <div v-else-if="status === 'done'" class="card">
      <div class="success">✓</div>
      <div class="title">已提交到电脑</div>
      <div class="hint">请查看电脑端处理结果</div>
      <button class="btn-secondary" @click="close">关闭页面</button>
    </div>

    <div v-else-if="status === 'error'" class="card">
      <div class="error">✗</div>
      <div class="title">出错了</div>
      <div class="hint-red">{{ errorMsg }}</div>
      <button class="btn-secondary" @click="reset">重新尝试</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const sessionId = ref('')
const status = ref<'ready' | 'recording' | 'uploading' | 'result' | 'done' | 'error'>('ready')
const transcript = ref('')
const errorMsg = ref('')
const duration = ref(0)
const progress = ref(0)

const fileInput = ref<HTMLInputElement | null>(null)
let recorder: MediaRecorder | null = null
let chunks: Blob[] = []
let timer: number | null = null

onMounted(() => {
  sessionId.value = route.query.session as string || ''
  if (!sessionId.value) {
    status.value = 'error'
    errorMsg.value = '缺少会话ID'
  }
})

const startRecord = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    recorder = new MediaRecorder(stream)
    chunks = []
    
    recorder.ondataavailable = (e) => chunks.push(e.data)
    recorder.onstop = () => {
      stream.getTracks().forEach(t => t.stop())
      const blob = new Blob(chunks, { type: 'audio/webm' })
      uploadAudio(blob)
    }
    
    recorder.start()
    status.value = 'recording'
    duration.value = 0
    timer = window.setInterval(() => {
      duration.value++
      if (duration.value >= 300) stopRecord()
    }, 1000)
  } catch {
    status.value = 'error'
    errorMsg.value = '无法访问麦克风，请检查权限'
  }
}

const stopRecord = () => {
  if (recorder && recorder.state !== 'inactive') {
    recorder.stop()
  }
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleUpload = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) uploadAudio(file)
}

const uploadAudio = async (blob: Blob) => {
  status.value = 'uploading'
  progress.value = 0
  
  const form = new FormData()
  form.append('audio', blob, 'record.webm')
  form.append('sessionId', sessionId.value)
  
  try {
    const res = await fetch('/api/voice-session/upload', {
      method: 'POST',
      body: form
    })
    
    const data = await res.json()
    if (data.code === 0) {
      transcript.value = data.data?.transcript || ''
      status.value = 'result'
    } else {
      status.value = 'error'
      errorMsg.value = data.message || '识别失败'
    }
  } catch {
    status.value = 'error'
    errorMsg.value = '上传失败，请检查网络'
  }
}

const reset = () => {
  status.value = 'ready'
  transcript.value = ''
  duration.value = 0
}

const submit = async () => {
  if (!transcript.value.trim()) {
    errorMsg.value = '请输入内容'
    return
  }
  
  try {
    const res = await fetch(`/api/voice-session/${sessionId.value}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ transcript: transcript.value })
    })
    
    const data = await res.json()
    if (data.code === 0) {
      status.value = 'done'
    } else {
      errorMsg.value = data.message || '提交失败'
      status.value = 'error'
    }
  } catch {
    errorMsg.value = '网络错误'
    status.value = 'error'
  }
}

const close = () => {
  window.close()
}
</script>

<style scoped>
.mobile-voice-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffb366, #ff9443);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  text-align: center;
  margin-bottom: 16px;
}

.header h1 {
  color: white;
  font-size: 20px;
  margin: 0 0 6px;
}

.session-badge {
  background: rgba(255,255,255,0.3);
  color: white;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.mic-icon { font-size: 56px; }

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.hint {
  font-size: 13px;
  color: #666;
}

.hint-red {
  font-size: 13px;
  color: #f56c6c;
}

.btn-primary,
.btn-secondary,
.btn-danger {
  width: 100%;
  padding: 14px;
  border-radius: 10px;
  font-size: 16px;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: linear-gradient(135deg, #ffb366, #ff9443);
  color: white;
}

.btn-secondary {
  background: #409eff;
  color: white;
}

.btn-danger {
  background: #f56c6c;
  color: white;
}

.upload-area {
  width: 100%;
}

.wave {
  display: flex;
  gap: 4px;
  height: 40px;
  align-items: center;
}

.wave span {
  width: 6px;
  height: 20px;
  background: #ffb366;
  border-radius: 3px;
  animation: wave 0.8s ease-in-out infinite;
}

.wave span:nth-child(2) { animation-delay: 0.1s; }
.wave span:nth-child(3) { animation-delay: 0.2s; }
.wave span:nth-child(4) { animation-delay: 0.3s; }
.wave span:nth-child(5) { animation-delay: 0.4s; }

@keyframes wave {
  0%, 100% { height: 20px; }
  50% { height: 40px; }
}

.timer {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #ddd;
  border-top-color: #ffb366;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.progress {
  font-size: 16px;
  color: #666;
}

.success, .error {
  width: 56px;
  height: 56px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.success { background: #67c23a; color: white; }
.error { background: #f56c6c; color: white; }

.transcript-area {
  width: 100%;
}

.transcript-area textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
}

.actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.actions button {
  flex: 1;
}
</style>
