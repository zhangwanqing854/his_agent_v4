<template>
  <el-dialog
    v-model="visible"
    title="科室人员关系导入"
    width="700px"
    :close-on-click-modal="false"
    class="import-dialog"
    @closed="handleClose"
  >
    <div class="import-content">
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".csv"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">拖拽文件到此处或点击上传</div>
        <div class="upload-hint">仅支持 CSV 格式，最大 1000 条数据</div>
      </el-upload>

      <div v-if="previewData.length > 0" class="preview-section">
        <div class="preview-header">
          <span>数据预览（前 10 行）</span>
          <el-tag type="info" size="small">{{ previewData.length }} 条数据</el-tag>
        </div>
        <el-table :data="previewData" border size="small" max-height="300">
          <el-table-column prop="codeUser" label="CODE_USER" width="150" />
          <el-table-column prop="nameUser" label="NAME_USER" />
          <el-table-column prop="codeDept" label="CODE_DEPT" width="150" />
          <el-table-column prop="nameDept" label="NAME_DEPT" />
        </el-table>
      </div>

      <div v-if="importing" class="progress-section">
        <el-progress :percentage="50" :indeterminate="true" />
        <span class="progress-text">正在导入...</span>
      </div>

      <div v-if="importResult" class="result-section">
        <el-result
          :icon="importResult.failCount === 0 ? 'success' : 'warning'"
          :title="importResult.failCount === 0 ? '导入成功' : '导入完成（部分失败）'"
        >
          <template #sub-title>
            <div class="result-stats">
              <span>总计: {{ importResult.totalCount }}</span>
              <span>新增: {{ importResult.insertCount }}</span>
              <span>跳过: {{ importResult.skipCount }}</span>
              <span>失败: {{ importResult.failCount }}</span>
            </div>
          </template>
          <template #extra>
            <el-button v-if="importResult.errors.length > 0" type="primary" @click="showErrorDetails">
              查看错误详情
            </el-button>
          </template>
        </el-result>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="previewData.length > 0 && !importResult"
          type="primary"
          :loading="importing"
          @click="handleImport"
        >
          确认导入
        </el-button>
        <el-button v-if="importResult" type="primary" @click="handleClose">
          完成
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog
    v-model="errorDialogVisible"
    title="错误详情"
    width="600px"
    class="error-dialog"
  >
    <el-table :data="importResult?.errors || []" border size="small">
      <el-table-column prop="lineNumber" label="行号" width="80" />
      <el-table-column prop="message" label="错误原因" />
    </el-table>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadInstance } from 'element-plus'
import { importDoctorDepartment, type ImportResult } from '@/api/doctorDepartment'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)
const previewData = ref<{ codeUser: string; nameUser: string; codeDept: string; nameDept: string }[]>([])
const importing = ref(false)
const importResult = ref<ImportResult | null>(null)
const errorDialogVisible = ref(false)

const handleClose = () => {
  visible.value = false
  selectedFile.value = null
  previewData.value = []
  importing.value = false
  importResult.value = null
  uploadRef.value?.clearFiles()
}

const handleFileChange = (uploadFile: UploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  if (!file.name.endsWith('.csv')) {
    ElMessage.error('仅支持 CSV 格式文件')
    uploadRef.value?.clearFiles()
    return
  }

  selectedFile.value = file
  parsePreview(file)
}

const parsePreview = async (file: File) => {
  try {
    const text = await file.text()
    const lines = text.split('\n').filter(line => line.trim())
    
    if (lines.length === 0) {
      ElMessage.error('文件为空')
      return
    }

    const headerLine = lines[0]
    if (!headerLine) {
      ElMessage.error('CSV 文件格式错误')
      return
    }
    const headers = headerLine.split(',').map(h => h.trim().replace(/^"|"$/g, '').toLowerCase())
    
    const codeUserIdx = headers.findIndex(h => h === 'code_user')
    const nameUserIdx = headers.findIndex(h => h === 'name_user')
    const codeDeptIdx = headers.findIndex(h => h === 'code_dept')
    const nameDeptIdx = headers.findIndex(h => h === 'name_dept')

    previewData.value = []
    for (let i = 1; i < Math.min(lines.length, 11); i++) {
      const line = lines[i]
      if (!line) continue
      const values = parseCsvLine(line)
      previewData.value.push({
        codeUser: codeUserIdx >= 0 ? values[codeUserIdx] || '' : '',
        nameUser: nameUserIdx >= 0 ? values[nameUserIdx] || '' : '',
        codeDept: codeDeptIdx >= 0 ? values[codeDeptIdx] || '' : '',
        nameDept: nameDeptIdx >= 0 ? values[nameDeptIdx] || '' : ''
      })
    }
  } catch (error) {
    ElMessage.error('文件解析失败')
  }
}

const parseCsvLine = (line: string): string[] => {
  const values: string[] = []
  let current = ''
  let inQuotes = false
  
  for (const c of line) {
    if (c === '"') {
      inQuotes = !inQuotes
    } else if (c === ',' && !inQuotes) {
      values.push(current.trim().replace(/^"|"$/g, ''))
      current = ''
    } else {
      current += c
    }
  }
  values.push(current.trim().replace(/^"|"$/g, ''))
  
  return values
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleImport = async () => {
  if (!selectedFile.value) return

  importing.value = true
  importResult.value = null

  try {
    const result = await importDoctorDepartment(selectedFile.value)
    importResult.value = result.data
    
    if (result.data.failCount === 0) {
      ElMessage.success(`导入成功：新增 ${result.data.insertCount} 条，跳过 ${result.data.skipCount} 条`)
    } else {
      ElMessage.warning(`导入完成：失败 ${result.data.failCount} 条`)
    }
    
    emit('success')
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

const showErrorDetails = () => {
  errorDialogVisible.value = true
}
</script>

<style>
.import-dialog .import-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.upload-area {
  border: 2px dashed var(--border-light);
  border-radius: 8px;
  background: var(--bg-secondary);
  padding: 30px;
  text-align: center;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: var(--color-primary-DEFAULT);
}

.upload-icon {
  font-size: 48px;
  color: var(--color-primary-DEFAULT);
}

.upload-text {
  font-size: 16px;
  color: var(--text-primary);
  margin-top: 10px;
}

.upload-hint {
  font-size: 13px;
  color: var(--text-placeholder);
  margin-top: 5px;
}

.preview-section {
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 15px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: 500;
}

.progress-section {
  text-align: center;
  padding: 20px;
}

.progress-text {
  display: block;
  margin-top: 10px;
  color: var(--text-secondary);
}

.result-section {
  padding: 10px;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>