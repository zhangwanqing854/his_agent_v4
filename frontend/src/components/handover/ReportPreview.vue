<template>
  <el-dialog
    v-model="visible"
    title=""
    width="1000px"
    :close-on-click-modal="false"
    @closed="handleClose"
    class="report-preview-dialog"
    :fullscreen="isFullscreen"
  >
    <div class="preview-toolbar no-print">
      <div class="toolbar-left">
        <span class="toolbar-title">交班报告预览</span>
        <el-tag :type="getStatusType(reportData.status)" size="small">
          {{ getStatusText(reportData.status) }}
        </el-tag>
      </div>
      <div class="toolbar-right">
        <el-button-group>
          <el-button @click="toggleFullscreen">
            <el-icon><FullScreen /></el-icon>
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </el-button>
          <el-button type="primary" @click="handlePrint">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </el-button-group>
        <el-button class="close-btn" @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="report-preview-wrapper" id="printable-area">
      <div class="report-preview-container">
        <div class="report-header">
          <h1>北京大学国际医院</h1>
          <h2>科室交班报告</h2>
        </div>

        <table class="info-table">
          <tr>
            <td class="label">科室</td>
            <td>{{ reportData.departmentName }}</td>
            <td class="label">日期</td>
            <td>{{ reportData.handoverDate }}</td>
            <td class="label">班次</td>
            <td>{{ reportData.shift }}</td>
          </tr>
        </table>

        <div class="section-title">交班详情</div>
        <div class="stats-text">
          全科患者 {{ reportData.stats.totalNum }}人，危重患者 {{ reportData.stats.diseNum }}人，新入院 {{ reportData.stats.newInHos }}人，
          转入 {{ reportData.stats.transIn }}人，转出 {{ reportData.stats.transOut }}人，今日出院 {{ reportData.stats.outNum }}人，
          今日手术 {{ reportData.stats.surgNum }}人，死亡 {{ reportData.stats.deathNum }}人
        </div>
        <table class="patient-table">
          <thead>
            <tr>
              <th style="width: 50px">床号</th>
              <th style="width: 60px">姓名</th>
              <th style="width: 40px">性别</th>
              <th style="width: 40px">年龄</th>
              <th style="width: 120px">诊断</th>
              <th style="width: 80px">生命体征</th>
              <th>病情描述</th>
              <th style="width: 80px">需观察项</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="patient in reportData.patients" :key="patient.id">
              <td>{{ patient.bedNumber }}</td>
              <td>{{ patient.name }}</td>
              <td>{{ patient.gender || '-' }}</td>
              <td>{{ patient.age ? patient.age + '岁' : '-' }}</td>
              <td>{{ patient.diagnosis }}</td>
              <td class="vitals-cell">{{ patient.vitals || '-' }}</td>
              <td>{{ patient.condition || '-' }}</td>
              <td>{{ patient.observationItems || '-' }}</td>
            </tr>
          </tbody>
        </table>

        <div class="signature-section">
          <div class="signature-row">
            <div class="signature-item">
              <span class="signature-label">交班医生签名：</span>
              <span class="signature-name">{{ reportData.fromDoctorName }}</span>
            </div>
            <div class="signature-item">
              <span class="signature-label">接班医生签名：</span>
              <span class="signature-name">{{ reportData.toDoctorName || '' }}</span>
            </div>
          </div>
        </div>

        <div class="report-footer">
          生成时间: {{ formatDateTime(reportData.printMetadata.generatedAt) }}
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer no-print">
        <el-button @click="handleClose">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button type="primary" @click="handlePrint">
          <el-icon><Printer /></el-icon>
          打印
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { FullScreen, Printer, Close, ArrowLeft } from '@element-plus/icons-vue'
import type { PrintableReport } from '@/mocks/handoverPrintData'

interface Props {
  modelValue: boolean
  reportData: PrintableReport
  mode?: 'preview' | 'view'
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), { mode: 'preview' })
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const isFullscreen = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  },
)
watch(visible, (val) => {
  emit('update:modelValue', val)
})

const getStatusType = (status: string): 'info' | 'warning' | 'primary' | 'success' => {
  const types: Record<string, 'info' | 'warning' | 'primary' | 'success'> = {
    DRAFT: 'info',
    PENDING: 'warning',
    TRANSFERRING: 'primary',
    COMPLETED: 'success',
  }
  return types[status] || 'info'
}

const getStatusText = (status: string): string => {
  const texts: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待交班',
    TRANSFERRING: '交班中',
    COMPLETED: '已完成',
  }
  return texts[status] || status
}

const formatDateTime = (dateStr: string): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const handlePrint = () => {
  const printContent = document.getElementById('printable-area')
  if (!printContent) {
    ElMessage.error('无法获取打印内容')
    return
  }

  const iframe = document.createElement('iframe')
  iframe.style.position = 'absolute'
  iframe.style.width = '0'
  iframe.style.height = '0'
  iframe.style.border = 'none'
  iframe.style.left = '-9999px'
  document.body.appendChild(iframe)

  const iframeDoc = iframe.contentWindow?.document
  if (!iframeDoc) {
    document.body.removeChild(iframe)
    ElMessage.error('打印失败')
    return
  }

  iframeDoc.open()
  iframeDoc.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>${props.reportData.departmentName}_${props.reportData.shift}_交班报告</title>
      <style>
        @page { size: A4 landscape; margin: 10mm; }
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; font-size: 11pt; color: #000; background: #fff; }
        .report-header { text-align: center; border-bottom: 1px solid #333; padding-bottom: 8px; margin-bottom: 10px; }
        .report-header h1 { font-size: 16pt; font-weight: bold; margin: 0; }
        .report-header h2 { font-size: 12pt; font-weight: normal; margin: 4px 0 0; color: #666; }
        .info-table { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
        .info-table td { padding: 5px 8px; border: 1px solid #999; font-size: 11pt; }
        .info-table .label { background: #f5f5f5; font-weight: bold; width: 70px; text-align: center; }
        .section-title { font-size: 13pt; font-weight: bold; color: #333; border-left: 3px solid #ffb366; padding-left: 8px; margin: 12px 0 6px; }
        .stats-text { margin: 6px 0; padding: 6px 8px; font-size: 11pt; color: #333; }
        .patient-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
        .patient-table th, .patient-table td { padding: 5px 8px; border: 1px solid #999; text-align: center; font-size: 11pt; }
        .patient-table th { background: #f5f5f5; font-weight: bold; }
        .patient-table td:nth-child(6) { text-align: left; font-size: 9pt; white-space: pre-line; }
        .patient-table td:nth-child(7) { text-align: left; }
        .patient-table td:nth-child(8) { text-align: left; }
        .signature-section { margin-top: 20px; padding-top: 15px; border-top: 1px solid #ddd; }
        .signature-row { display: flex; justify-content: space-between; gap: 40px; }
        .signature-item { display: flex; align-items: center; gap: 8px; }
        .signature-label { font-size: 12pt; font-weight: bold; color: #333; }
        .signature-name { font-size: 12pt; color: #000; min-width: 80px; }
        .report-footer { margin-top: 15px; padding-top: 8px; border-top: 1px solid #ddd; font-size: 10pt; color: #999; text-align: center; }
      </style>
    </head>
    <body>
      ${printContent.innerHTML}
    </body>
    </html>
  `)
  iframeDoc.close()

  setTimeout(() => {
    try {
      iframe.contentWindow?.focus()
      iframe.contentWindow?.print()
      ElMessage.success('打印对话框已打开')
    } catch {
      ElMessage.error('打印失败')
    }
    setTimeout(() => {
      document.body.removeChild(iframe)
    }, 1000)
  }, 500)
}

const handleClose = () => {
  visible.value = false
  emit('close')
}
</script>

<style scoped>
.report-preview-dialog :deep(.el-dialog__header) {
  display: none;
}
.report-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}
.report-preview-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid #e4e7ed;
  padding: 16px 24px;
}

.preview-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: #ffb366;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.toolbar-title {
  font-size: 16px;
  font-weight: 600;
  color: white;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.close-btn {
  padding: 8px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
}
.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.report-preview-wrapper {
  background: #f5f7fa;
  padding: 15px;
}
.report-preview-container {
  background: #fff;
  padding: 20px;
  max-width: 950px;
  margin: 0 auto;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.report-header {
  text-align: center;
  border-bottom: 1px solid #333;
  padding-bottom: 8px;
  margin-bottom: 10px;
}
.report-header h1 {
  font-size: 16px;
  font-weight: bold;
  margin: 0;
  color: #000;
}
.report-header h2 {
  font-size: 12px;
  font-weight: normal;
  margin: 4px 0 0;
  color: #666;
}

.section-title {
  font-size: 13px;
  font-weight: bold;
  color: #333;
  border-left: 3px solid #ffb366;
  padding-left: 8px;
  margin: 12px 0 6px;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 11px;
}

.info-table {
  margin-bottom: 10px;
}
.info-table td {
  padding: 5px 8px;
  border: 1px solid #999;
}
.info-table .label {
  background: #f5f5f5;
  font-weight: bold;
  width: 70px;
  text-align: center;
}

.stats-text {
  margin: 6px 0;
  padding: 6px 8px;
  font-size: 11px;
  color: #333;
}

.patient-table {
  margin-bottom: 20px;
}
.patient-table th,
.patient-table td {
  padding: 6px 8px;
  border: 1px solid #999;
  text-align: center;
}
.patient-table th {
  background: #f5f5f5;
  font-weight: bold;
}
.patient-table td:nth-child(7) {
  text-align: left;
}
.vitals-cell {
  font-size: 10px;
  text-align: left;
  white-space: pre-line;
}

.signature-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ddd;
}

.signature-row {
  display: flex;
  justify-content: space-between;
  gap: 40px;
}

.signature-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.signature-label {
  font-size: 12px;
  font-weight: bold;
  color: #333;
}

.signature-name {
  font-size: 12px;
  color: #000;
  min-width: 80px;
}

.report-footer {
  margin-top: 15px;
  padding-top: 8px;
  border-top: 1px solid #ddd;
  font-size: 10px;
  color: #999;
  text-align: center;
}
</style>
