<template>
  <el-dialog
    v-model="visible"
    title=""
    width="1100px"
    :close-on-click-modal="false"
    @closed="handleClose"
    @open="handleOpen"
    class="handover-dialog"
  >
    <div class="handover-container">
      <!-- 头部信息 -->
      <div class="header-section">
        <div class="header-title">
          <div class="title-icon">📋</div>
          <div class="title-text">
            <h2>发起交班</h2>
            <p>填写交班信息，完成科室交接</p>
          </div>
        </div>
        <div class="header-info">
          <div class="info-badge">
            <span class="badge-label">科室</span>
            <span class="badge-value">{{ authStore.currentDepartmentName }}</span>
          </div>
        </div>
      </div>

      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="info-grid">
          <div class="info-field">
            <label>班次</label>
            <el-radio-group v-model="form.shift" class="shift-radio">
              <el-radio-button value="白班">☀️ 白班</el-radio-button>
              <el-radio-button value="夜班">🌙 夜班</el-radio-button>
            </el-radio-group>
          </div>
          <div class="info-field">
            <label>接班医生 <span class="required">*</span></label>
            <el-select v-model="form.toDoctorId" placeholder="请选择接班医生" class="doctor-select">
              <el-option
                v-for="doctor in filteredDoctorList"
                :key="doctor.id"
                :label="doctor.name"
                :value="doctor.id"
              />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 科室统计 -->
      <div class="stats-section">
        <div class="stats-row">
          <div class="stat-item primary">
            <span class="stat-label">患者总数</span>
            <span class="stat-value">{{ form.patients.length }}人</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">入院</span>
            <el-input-number v-model="form.stats.admission" :min="0" size="small" controls-position="right" />
          </div>
          <div class="stat-item">
            <span class="stat-label">转出</span>
            <el-input-number v-model="form.stats.transferOut" :min="0" size="small" controls-position="right" />
          </div>
          <div class="stat-item">
            <span class="stat-label">出院</span>
            <el-input-number v-model="form.stats.discharge" :min="0" size="small" controls-position="right" />
          </div>
          <div class="stat-item">
            <span class="stat-label">转入</span>
            <el-input-number v-model="form.stats.transferIn" :min="0" size="small" controls-position="right" />
          </div>
          <div class="stat-item danger">
            <span class="stat-label">死亡</span>
            <el-input-number v-model="form.stats.death" :min="0" size="small" controls-position="right" />
          </div>
          <div class="stat-item">
            <span class="stat-label">手术</span>
            <el-input-number v-model="form.stats.surgery" :min="0" size="small" controls-position="right" />
          </div>
          <div class="stat-item warning">
            <span class="stat-label">病危</span>
            <span class="stat-value">{{ criticalCount }}人</span>
          </div>
        </div>
      </div>

      <!-- 患者列表 -->
      <div class="patient-card">
        <el-table
          :data="form.patients"
          style="width: 100%"
          class="patient-table"
          :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
        >
          <el-table-column prop="bedNumber" label="床号" width="80" align="center">
            <template #default="{ row }">
              <span class="bed-tag">{{ row.bedNumber }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="100" align="center">
            <template #default="{ row }">
              <span class="patient-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="diagnosis" label="诊断" min-width="180">
            <template #default="{ row }">
              <span class="diagnosis-text">{{ row.diagnosis }}</span>
            </template>
          </el-table-column>
          <el-table-column label="病情" min-width="320">
            <template #default="{ row }">
              <el-input
                v-model="row.condition"
                type="textarea"
                :rows="2"
                placeholder="请输入病情描述..."
                class="condition-input"
                :class="{ 'has-conflict': row.hasConflict }"
                resize="none"
              />
              <div v-if="row.hasConflict" class="conflict-tip">
                <el-icon><WarningFilled /></el-icon>
                内容已修改，请确认
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 语音录入按钮 -->
        <div class="voice-input-section">
          <el-button type="primary" plain @click="showVoiceDialog = true">
            <el-icon><Microphone /></el-icon>
            批量语音录入
          </el-button>
          <span class="voice-hint">支持床号+姓名识别，自动匹配患者</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" class="cancel-btn">取消</el-button>
        <el-button type="info" plain @click="handlePreview" class="preview-btn">
          <el-icon><Document /></el-icon>
          预览报告
        </el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit" class="submit-btn">
          <el-icon v-if="!loading"><Check /></el-icon>
          {{ loading ? '提交中...' : '确认发起交班' }}
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 语音录入弹窗 -->
  <VoiceInputDialog
    v-model="showVoiceDialog"
    :patients="form.patients"
    @apply="handleVoiceApply"
  />

  <!-- 报告预览弹窗 -->
  <ReportPreview
    v-if="previewData"
    v-model="showPreviewDialog"
    :report-data="previewData"
    mode="preview"
    @close="showPreviewDialog = false"
  />
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Microphone, WarningFilled, Document } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import VoiceInputDialog from './VoiceInputDialog.vue'
import ReportPreview from './ReportPreview.vue'
import { generatePreviewData, type PrintableReport } from '@/mocks/handoverPrintData'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

interface Doctor {
  id: number
  name: string
  departmentId: number
}

interface PatientHandover {
  id: number
  bedNumber: string
  name: string
  diagnosis: string
  condition: string
  hasConflict?: boolean
}

interface VoiceResult {
  patientId: number
  mergedContent: string
  hasConflict: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const authStore = useAuthStore()

const visible = ref(props.modelValue)
const loading = ref(false)
const showVoiceDialog = ref(false)
const showPreviewDialog = ref(false)
const previewData = ref<PrintableReport | null>(null)

const allDoctors: Doctor[] = [
  { id: 2, name: '李医生', departmentId: 1 },
  { id: 3, name: '王医生', departmentId: 2 },
  { id: 4, name: '赵医生', departmentId: 3 },
  { id: 5, name: '钱医生', departmentId: 4 },
  { id: 6, name: '孙医生', departmentId: 1 },
  { id: 7, name: '周医生', departmentId: 2 }
]

const filteredDoctorList = computed(() => {
  const deptId = authStore.currentDepartmentId
  return allDoctors.filter(d => d.departmentId === deptId)
})

const patientsByDepartment: Record<number, PatientHandover[]> = {
  1: [
    { id: 1, bedNumber: '2床', name: '张三', diagnosis: '感冒', condition: '主诉头痛、恶心、呕吐、视物模糊1周。入院查体，神志清楚，双侧瞳孔等大等圆，对光反射迟钝。' },
    { id: 2, bedNumber: '17床', name: '李四', diagnosis: '发烧', condition: '主诉头痛、恶心、呕吐、视物模糊1周。入院查体。' },
    { id: 3, bedNumber: '21床', name: '王五', diagnosis: '头痛', condition: '主诉头痛、恶心、呕吐、视物模糊1周。入院查体，神志清楚，双侧瞳孔等大等圆。' },
    { id: 4, bedNumber: '5床', name: '赵六', diagnosis: '高血压2级', condition: '' },
    { id: 5, bedNumber: '8床', name: '钱七', diagnosis: '2型糖尿病', condition: '' },
    { id: 6, bedNumber: '12床', name: '孙八', diagnosis: '心律失常', condition: '' },
    { id: 7, bedNumber: '15床', name: '周九', diagnosis: '心力衰竭', condition: '' },
    { id: 8, bedNumber: '20床', name: '吴十', diagnosis: '高血压1级', condition: '' }
  ],
  2: [
    { id: 11, bedNumber: '1床', name: '郑十一', diagnosis: '脑梗死', condition: '肢体无力加重，需密切观察。' },
    { id: 12, bedNumber: '3床', name: '王十二', diagnosis: '脑出血', condition: '术后第1天，意识清楚。' },
    { id: 13, bedNumber: '5床', name: '陈十三', diagnosis: '癫痫持续状态', condition: '今日发作2次，已用药控制。' },
    { id: 14, bedNumber: '7床', name: '林十四', diagnosis: '重症肌无力', condition: '呼吸肌无力，密切监测。' },
    { id: 15, bedNumber: '9床', name: '黄十五', diagnosis: '偏头痛', condition: '' }
  ],
  3: [
    { id: 21, bedNumber: '2床', name: '孙八', diagnosis: 'COPD急性加重', condition: '呼吸困难加重，已给予氧疗。' },
    { id: 22, bedNumber: '5床', name: '周九', diagnosis: '肺炎、呼吸衰竭', condition: '氧饱和度波动，持续监测。' },
    { id: 23, bedNumber: '8床', name: '吴十', diagnosis: '支气管哮喘', condition: '' }
  ],
  4: [
    { id: 31, bedNumber: '1床', name: '郑十', diagnosis: '甲状腺功能亢进', condition: '' },
    { id: 32, bedNumber: '4床', name: '王十一', diagnosis: '糖尿病肾病', condition: '肾功能监测中。' }
  ]
}

const form = reactive({
  shift: '白班',
  toDoctorId: null as number | null,
  patients: [] as PatientHandover[],
  stats: {
    admission: 1,
    transferOut: 1,
    discharge: 1,
    transferIn: 0,
    death: 0,
    surgery: 0
  }
})

const criticalCount = computed(() => {
  return form.patients.filter(p => p.condition && p.condition.length > 0).length
})

watch(
  () => props.modelValue,
  (val) => { visible.value = val }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleOpen = () => {
  const deptId = authStore.currentDepartmentId
  if (deptId && patientsByDepartment[deptId]) {
    form.patients = JSON.parse(JSON.stringify(patientsByDepartment[deptId]))
  } else {
    form.patients = []
  }
  form.shift = '白班'
  form.toDoctorId = null
  form.stats = {
    admission: 1,
    transferOut: 1,
    discharge: 1,
    transferIn: 0,
    death: 0,
    surgery: 0
  }
}

const resetForm = () => {
  form.shift = '白班'
  form.toDoctorId = null
  form.patients = []
  form.stats = {
    admission: 0,
    transferOut: 0,
    discharge: 0,
    transferIn: 0,
    death: 0,
    surgery: 0
  }
}

const handleClose = () => {
  visible.value = false
  resetForm()
}

// 处理语音录入结果
const handleVoiceApply = (results: VoiceResult[]) => {
  for (const result of results) {
    const patient = form.patients.find(p => p.id === result.patientId)
    if (patient) {
      patient.condition = result.mergedContent
      patient.hasConflict = result.hasConflict
    }
  }
}

// 预览报告
const handlePreview = () => {
  // 验证必要字段
  if (!form.toDoctorId) {
    ElMessage.warning('请先选择接班医生')
    return
  }

  if (form.patients.length === 0) {
    ElMessage.warning('当前科室暂无患者')
    return
  }

  // 生成预览数据
  const data = generatePreviewData(form)
  previewData.value = data
  showPreviewDialog.value = true
}

const handleSubmit = async () => {
  if (!form.toDoctorId) {
    ElMessage.warning('请选择接班医生')
    return
  }

  if (form.patients.length === 0) {
    ElMessage.warning('当前科室暂无患者，无法发起交班')
    return
  }

  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))

    const handoverData = {
      departmentId: authStore.currentDepartmentId,
      departmentName: authStore.currentDepartmentName,
      shift: form.shift,
      toDoctorId: form.toDoctorId,
      stats: { ...form.stats, totalPatients: form.patients.length },
      patients: form.patients.map(p => ({
        id: p.id,
        bedNumber: p.bedNumber,
        name: p.name,
        diagnosis: p.diagnosis,
        condition: p.condition
      }))
    }

    console.log('发起科室交班:', handoverData)
    ElMessage.success('交班发起成功')
    emit('success')
    handleClose()
  } catch {
    ElMessage.error('交班发起失败')
  } finally {
    loading.value = false
  }
}
</script>

<style>
.handover-dialog .el-dialog__body {
  padding: 0;
}

.handover-dialog .el-dialog__header {
  display: none;
}

.handover-container {
  padding: 0;
}

/* 头部区域 */
.header-section {
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 12px 12px 0 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  font-size: 40px;
  background: rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 12px;
}

.title-text h2 {
  color: white;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.title-text p {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  margin: 0;
}

.info-badge {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 12px 20px;
  border-radius: 30px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.badge-value {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

/* 卡片通用样式 */
.info-card, .stats-card, .patient-card {
  background: white;
  margin: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 基本信息卡片 */
.info-grid {
  padding: 20px;
  display: flex;
  gap: 40px;
}

.info-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-field label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.required {
  color: #f56c6c;
}

.shift-radio {
  display: flex;
  gap: 12px;
}

.shift-radio .el-radio-button__inner {
  padding: 10px 24px;
  border-radius: 8px !important;
  border: 1px solid #dcdfe6;
}

.shift-radio .el-radio-button__original-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  border-color: #ffb366;
  box-shadow: 0 2px 8px rgba(255, 179, 102, 0.4);
}

.shift-icon {
  margin-right: 6px;
}

.doctor-select {
  width: 200px;
}

.doctor-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.doctor-avatar {
  font-size: 16px;
}

/* 科室统计 */
.stats-section {
  margin: 0 24px 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border-radius: 12px;
  border: 1px solid rgba(255, 179, 102, 0.2);
}

.stats-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-item .stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-item .stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-item.primary .stat-value {
  color: #ea580c;
  font-size: 18px;
}

.stat-item.warning .stat-value {
  color: #d97706;
}

.stat-item.danger .stat-label {
  color: #dc2626;
}

.stat-item .el-input-number {
  width: 70px;
}

.stat-item .el-input-number .el-input__wrapper {
  background: white;
  border-radius: 6px;
}

/* 患者列表 */
.patient-card {
  margin-bottom: 0;
  border-radius: 12px;
}

.patient-table {
  border-radius: 0 0 12px 12px;
}

.patient-table .el-table__header th {
  background: #fafafa !important;
}

.bed-tag {
  display: inline-block;
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.patient-name {
  font-weight: 500;
  color: #303133;
}

.diagnosis-text {
  color: #606266;
  font-size: 14px;
}

.condition-input .el-textarea__inner {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.condition-input .el-textarea__inner:focus {
  border-color: #ffb366;
  box-shadow: 0 0 0 2px rgba(255, 179, 102, 0.2);
}

.condition-input.has-conflict .el-textarea__inner {
  border-color: #f56c6c;
  background: #fff1f0;
}

.conflict-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 12px;
  color: #f56c6c;
}

/* 语音输入区域 */
.voice-input-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
}

.voice-hint {
  font-size: 13px;
  color: #909399;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  background: #fafafa;
  border-radius: 0 0 12px 12px;
}

.cancel-btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
}

.submit-btn {
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 14px;
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 179, 102, 0.4);
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 179, 102, 0.5);
}
</style>