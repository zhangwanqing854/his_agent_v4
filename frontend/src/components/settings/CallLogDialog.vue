<template>
  <el-dialog
    v-model="visible"
    title="调用日志详情"
    width="800px"
    class="log-dialog"
    @closed="handleClose"
  >
    <div v-if="log" class="log-detail">
      <!-- 基本信息 -->
      <div class="detail-section">
        <div class="section-header">
          <el-icon class="section-icon"><InfoFilled /></el-icon>
          基本信息
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">接口名称</span>
            <span class="info-value">{{ log.configName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">调用时间</span>
            <span class="info-value">{{ log.callTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">调用状态</span>
            <el-tag :type="getStatusTagType(log.status)" effect="light" size="small">
              {{ statusLabelMap[log.status] }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">耗时</span>
            <span class="info-value">{{ log.duration }}ms</span>
          </div>
        </div>
      </div>

      <!-- 错误信息 -->
      <div v-if="log.errorMessage" class="detail-section error-section">
        <div class="section-header error">
          <el-icon class="section-icon"><WarningFilled /></el-icon>
          错误信息
        </div>
        <div class="error-content">
          {{ log.errorMessage }}
        </div>
      </div>

      <!-- 请求内容 -->
      <div class="detail-section">
        <div class="section-header">
          <el-icon class="section-icon"><Promotion /></el-icon>
          请求内容
        </div>
        <div class="code-block">
          <pre>{{ formatJson(log.request) }}</pre>
        </div>
      </div>

      <!-- 响应内容 -->
      <div class="detail-section">
        <div class="section-header">
          <el-icon class="section-icon"><Download /></el-icon>
          响应内容
        </div>
        <div v-if="log.response" class="code-block">
          <pre>{{ formatJson(log.response) }}</pre>
        </div>
        <div v-else class="empty-response">
          <el-icon :size="24"><Warning /></el-icon>
          <span>无响应内容</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleCopyLog">
          <el-icon><CopyDocument /></el-icon>
          复制日志
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, WarningFilled, Promotion, Download, Warning, CopyDocument } from '@element-plus/icons-vue'

interface CallLog {
  id: number
  configId: number
  configName: string
  callTime: string
  duration: number
  status: 'SUCCESS' | 'FAILED' | 'TIMEOUT'
  request: string
  response: string
  errorMessage?: string
}

interface Props {
  modelValue: boolean
  log: CallLog | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)

const statusLabelMap: Record<string, string> = {
  SUCCESS: '成功',
  FAILED: '失败',
  TIMEOUT: '超时'
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const getStatusTagType = (status: string): '' | 'success' | 'warning' | 'info' | 'danger' => {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    SUCCESS: 'success',
    FAILED: 'danger',
    TIMEOUT: 'warning'
  }
  return map[status] || ''
}

const formatJson = (str: string): string => {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const handleCopyLog = () => {
  if (!props.log) return

  const logText = `
调用日志详情
================
接口名称: ${props.log.configName}
调用时间: ${props.log.callTime}
调用状态: ${statusLabelMap[props.log.status]}
耗时: ${props.log.duration}ms
${props.log.errorMessage ? `\n错误信息: ${props.log.errorMessage}` : ''}

请求内容:
${formatJson(props.log.request)}

响应内容:
${formatJson(props.log.response)}
  `.trim()

  navigator.clipboard.writeText(logText).then(() => {
    ElMessage.success('日志已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style>
.log-dialog .el-dialog__body {
  padding: 16px 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.log-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 16px;
}

.section-header {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-header.error {
  color: var(--color-danger);
}

.section-icon {
  color: var(--color-primary-DEFAULT);
}

.section-header.error .section-icon {
  color: var(--color-danger);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-label {
  font-size: 14px;
  color: var(--text-secondary);
  min-width: 70px;
}

.info-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.error-section {
  background: #fff1f0;
  border: 1px solid #ffa39e;
}

.error-content {
  font-size: 14px;
  color: var(--color-danger);
  line-height: 1.6;
}

.code-block {
  background: #1e1e1e;
  border-radius: var(--radius-sm);
  padding: 12px;
  overflow-x: auto;
}

.code-block pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #d4d4d4;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.empty-response {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>