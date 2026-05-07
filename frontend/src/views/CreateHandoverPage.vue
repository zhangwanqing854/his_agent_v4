<template>
  <div class="create-handover-page">
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.push('/handovers')">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>{{ isEditMode ? '编辑交班' : '发起交班' }} 
          <span class="dept-badge">{{ authStore.currentDepartmentName }}</span>
          <span v-if="isEditMode && form.handoverNo" class="handover-no-badge">
            编号: {{ form.handoverNo }}
          </span>
        </h1>
      </div>
      <div class="header-right">
        <el-button type="info" plain @click="handlePreview">
          <el-icon><Document /></el-icon>
          预览报告
        </el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          <el-icon v-if="!submitting"><Check /></el-icon>
          {{ submitting ? '提交中...' : isEditMode ? '保存修改' : '确认发起交班' }}
        </el-button>
      </div>
    </div>

    <div class="page-content">
      <div class="form-section">
        <div class="section-title">
          <el-icon><Setting /></el-icon>
          基本信息
        </div>
        <div class="form-row">
          <div class="form-item">
            <label>班次</label>
            <el-radio-group v-model="form.shift" class="shift-radio">
              <el-radio-button value="白班">☀️ 白班</el-radio-button>
              <el-radio-button value="夜班">🌙 夜班</el-radio-button>
            </el-radio-group>
          </div>
          <div class="form-item">
            <label>接班医生 <span class="required">*</span></label>
            <el-select v-model="form.toDoctorId" placeholder="请选择接班医生" style="width: 200px" filterable>
              <el-option
                v-for="doctor in doctorList"
                :key="doctor.id"
                :label="doctor.name"
                :value="doctor.id"
              />
            </el-select>
            <span v-if="noDutyStaffMsg" class="no-duty-tip">{{ noDutyStaffMsg }}</span>
          </div>
        </div>
      </div>

      <div class="stats-section">
        <div class="stats-row">
          <div class="stat-item primary">
            <span class="stat-label">全科患者</span>
            <span class="stat-value">{{ stats.totalNum }}人</span>
          </div>
          <div class="stat-item warning">
            <span class="stat-label">危重患者</span>
            <span class="stat-value">{{ stats.diseNum }}人</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">新入院</span>
            <span class="stat-value">{{ stats.newInHos }}人</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">转入</span>
            <span class="stat-value">{{ stats.transIn }}人</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">转出</span>
            <span class="stat-value">{{ stats.transOut }}人</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">今日出院</span>
            <span class="stat-value">{{ stats.outNum }}人</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">今日手术</span>
            <span class="stat-value">{{ stats.surgNum }}人</span>
          </div>
          <div class="stat-item danger">
            <span class="stat-label">死亡</span>
            <span class="stat-value">{{ stats.deathNum }}人</span>
          </div>
        </div>
        <div v-if="deptPatientOverview?.syncedAt" class="sync-time">
          数据同步时间：{{ new Date(deptPatientOverview.syncedAt).toLocaleString('zh-CN') }}
        </div>
      </div>

      <div class="patient-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon><User /></el-icon>
            患者列表
          </div>
          <el-button type="primary" plain size="small" @click="showVoiceDialog = true">
            <el-icon><Microphone /></el-icon>
            批量语音录入
          </el-button>
        </div>

        <el-table
          :data="form.patients"
          style="width: 100%"
          class="patient-table"
          :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
          border
        >
          <el-table-column prop="bedNumber" label="床号" width="80" align="center">
            <template #default="{ row }">
              <span class="bed-tag">{{ row.bedNumber }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="100" align="center">
            <template #default="{ row }">
              <span class="patient-name">{{ row.name }}</span>
              <el-tag v-if="row.isCritical" type="danger" size="small" style="margin-left: 4px">危</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="70" align="center">
            <template #default="{ row }">
              <span>{{ row.age }}岁</span>
            </template>
          </el-table-column>
          <el-table-column prop="gender" label="性别" width="60" align="center" />
          <el-table-column prop="diagnosis" label="诊断" min-width="150" show-overflow-tooltip />
          <el-table-column prop="filterReason" label="筛选原因" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.filterReason" type="warning" size="small">{{ row.filterReason }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="生命体征" min-width="100">
            <template #default="{ row }">
              <el-input
                v-model="row.vitals"
                type="textarea"
                autosize
                placeholder="请输入生命体征..."
                resize="none"
                @blur="savePatientField(row, 'vitals')"
              />
            </template>
          </el-table-column>
          <el-table-column label="目前情况" min-width="250">
            <template #default="{ row }">
              <el-input
                v-model="row.currentCondition"
                type="textarea"
                autosize
                placeholder="请输入目前情况..."
                resize="none"
                @blur="savePatientField(row, 'currentCondition')"
              />
            </template>
          </el-table-column>
          <el-table-column label="需观察项" min-width="90">
            <template #default="{ row }">
              <el-input
                v-model="row.observationItems"
                type="textarea"
                autosize
                placeholder="请输入需观察项..."
                resize="none"
                @blur="savePatientField(row, 'observationItems')"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                :icon="Microphone" 
                circle 
                size="small"
                @click="openInlineVoice(row)"
              />
              <el-button 
                type="success" 
                size="small"
                @click="openExamReport(row)"
              >
                检查
              </el-button>
              <el-button 
                type="warning" 
                size="small"
                @click="openTestReport(row)"
              >
                检验
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <VoiceInputDialog
      v-model="showVoiceDialog"
      :patients="voiceDialogPatients"
      :patient="inlineVoicePatient"
      @apply="handleVoiceApply"
      @batchApply="handleBatchVoiceApply"
      @inlineApply="handleInlineVoiceApply"
    />

    <ReportPreview
      v-if="previewData"
      v-model="showPreviewDialog"
      :report-data="previewData"
      mode="preview"
      @close="showPreviewDialog = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Document, Setting, User, Microphone } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { fetchHandoverStats, fetchDutyStaff, fetchDutyStaffList, fetchHandoverPatientsForCreate, createHandover, fetchHandoverById, fetchHandoverPatients, updateHandoverPatient, type HandoverStatsDto, type DutyStaffDto, type HandoverPatientDto } from '@/api/handover'
import { fetchDeptPatientOverview, type DeptPatientOverview } from '@/api/deptPatientOverview'
import type { HandoverForm } from '@/types/patient'
import VoiceInputDialog from '@/components/handover/VoiceInputDialog.vue'
import ReportPreview from '@/components/handover/ReportPreview.vue'
import { generatePreviewData, type PrintableReport } from '@/mocks/handoverPrintData'
import { fetchSystemConfig, type SystemConfig } from '@/api/systemConfig'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isEditMode = computed(() => route.name === 'EditHandover')
const handoverId = computed(() => route.params.id ? Number(route.params.id) : null)

const submitting = ref(false)
const showVoiceDialog = ref(false)
const showPreviewDialog = ref(false)
const previewData = ref<PrintableReport | null>(null)
const noDutyStaffMsg = ref('')
const inlineVoicePatient = ref<any>(null)
const deptPatientOverview = ref<DeptPatientOverview | null>(null)
const systemConfig = ref<SystemConfig | null>(null)

interface Doctor {
  id: number
  name: string
}

const doctorList = ref<Doctor[]>([])

const stats = reactive({
  totalNum: 0,
  diseNum: 0,
  newInHos: 0,
  transIn: 0,
  transOut: 0,
  outNum: 0,
  surgNum: 0,
  deathNum: 0
})

const form = reactive<HandoverForm>({
  handoverNo: null,
  shift: '夜班',
  toDoctorId: null,
  patients: [],
  stats: {
    totalNum: 0,
    diseNum: 0,
    newInHos: 0,
    transIn: 0,
    transOut: 0,
    outNum: 0,
    surgNum: 0,
    deathNum: 0
  }
})

const criticalCount = computed(() => {
  return form.patients.filter(p => p.isCritical).length
})

const voiceDialogPatients = computed(() => {
  return form.patients.map(p => ({
    id: p.id,
    bedNumber: p.bedNumber,
    name: p.name,
    diagnosis: p.diagnosis,
    condition: p.currentCondition
  }))
})

const loadStats = async () => {
  const deptCode = authStore.currentDepartmentCode
  if (deptCode) {
    try {
      const res = await fetchDeptPatientOverview(deptCode)
      if (res.data) {
        deptPatientOverview.value = res.data
        stats.totalNum = res.data.totalNum || 0
        stats.diseNum = res.data.diseNum || 0
        stats.newInHos = res.data.newInHos || 0
        stats.transIn = res.data.transIn || 0
        stats.transOut = res.data.transOut || 0
        stats.outNum = res.data.outNum || 0
        stats.surgNum = res.data.surgNum || 0
        stats.deathNum = res.data.deathNum || 0
      }
    } catch {
      stats.totalNum = 0
      stats.diseNum = 0
      stats.newInHos = 0
      stats.transIn = 0
      stats.transOut = 0
      stats.outNum = 0
      stats.surgNum = 0
      stats.deathNum = 0
      deptPatientOverview.value = null
    }
  }
}

const loadSystemConfig = async () => {
  try {
    const res = await fetchSystemConfig()
    if (res.data) {
      systemConfig.value = res.data
    }
  } catch (error) {
    console.error('加载系统配置失败:', error)
  }
}

const openExamReport = (row: any) => {
  if (!systemConfig.value?.EXAM_REPORT_URL) {
    ElMessage.warning('检查报告URL未配置')
    return
  }
  const url = systemConfig.value.EXAM_REPORT_URL.replace('{{:patient_no}}', row.patientNo)
  window.open(url, '_blank')
}

const openTestReport = (row: any) => {
  if (!systemConfig.value?.TEST_REPORT_URL) {
    ElMessage.warning('检验报告URL未配置')
    return
  }
  const url = systemConfig.value.TEST_REPORT_URL
    .replace('{{:patient_no}}', row.patientNo)
    .replace('{{:userId}}', authStore.userCode || '')
  window.open(url, '_blank')
}

const loadDutyStaff = async () => {
  const deptId = authStore.currentDepartmentId
  if (!deptId) return

  // 1. 获取科室值班人员列表（候选队列）
  try {
    const listRes = await fetchDutyStaffList()
    if (listRes.code === 0 && listRes.data && listRes.data.length > 0) {
      doctorList.value = listRes.data.map(d => ({
        id: d.staffId,
        name: d.staffName
      }))
    } else {
      noDutyStaffMsg.value = '请先配置科室值班人员'
      return
    }
  } catch {
    noDutyStaffMsg.value = '获取科室值班人员失败'
    return
  }

  // 2. 获取当天值班医生（默认值）
  try {
    const res = await fetchDutyStaff(deptId)
    if (res.code === 0 && res.data) {
      form.toDoctorId = res.data.staffId
      noDutyStaffMsg.value = ''
    } else {
      noDutyStaffMsg.value = '请先排班，否则无法发起交班'
      form.toDoctorId = null
    }
  } catch {
    noDutyStaffMsg.value = '请先排班，否则无法发起交班'
    form.toDoctorId = null
  }
}

const loadPatients = async () => {
  const deptId = authStore.currentDepartmentId
  if (deptId) {
    const res = await fetchHandoverPatientsForCreate(deptId)
    if (res.code === 0) {
      form.patients = res.data.map(p => ({
        id: p.id || p.visitId,
        bedNumber: p.bedNo || '',
        name: p.patientName,
        age: p.age,
        gender: p.gender,
        diagnosis: p.diagnosis,
        isCritical: false,
        vitals: p.vitals || '',
        currentCondition: p.currentCondition || '',
        observationItems: '',
        filterReason: p.filterReason,
        visitId: p.visitId,
        visitNo: p.visitNo,
        patientNo: p.patientNo
      }))
    }
  }
}

const handleVoiceApply = (results: any[]) => {
  results.forEach(result => {
    const patient = form.patients.find(p => p.id === result.patientId)
    if (patient) {
      patient.observationItems = result.mergedContent || result.newContent
    }
  })
}

const handleBatchVoiceApply = (items: any[]) => {
  items.forEach(item => {
    const patientId = item.selectedPatientId || item.matchResult?.patientId
    const patient = form.patients.find(p => p.id === patientId)
    if (patient) {
      patient.observationItems = item.parsedItem?.content || ''
    }
  })
}

const openInlineVoice = (patient: any) => {
  inlineVoicePatient.value = {
    id: patient.id,
    bedNumber: patient.bedNumber,
    name: patient.name,
    diagnosis: patient.diagnosis,
    condition: patient.currentCondition
  }
  showVoiceDialog.value = true
}

const handleInlineVoiceApply = async (content: string) => {
  if (inlineVoicePatient.value) {
    const patient = form.patients.find(p => p.id === inlineVoicePatient.value.id)
    if (patient) {
      patient.observationItems = content
      if (isEditMode.value && handoverId.value) {
        await savePatientField(patient, 'observationItems')
      }
      ElMessage.success(`已录入${patient.bedNumber} ${patient.name}的需观察项`)
    }
  }
  inlineVoicePatient.value = null
}

const savePatientField = async (row: any, field: string) => {
  if (!isEditMode.value || !handoverId.value) return
  
  try {
    const res = await updateHandoverPatient(handoverId.value, row.id, {
      vitals: row.vitals,
      currentCondition: row.currentCondition,
      observationItems: row.observationItems
    })
    if (res.code !== 0) {
      ElMessage.error('保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

const handlePreview = () => {
  if (!form.toDoctorId) {
    ElMessage.warning('请先选择接班医生')
    return
  }

  if (form.patients.length === 0) {
    ElMessage.warning('当前科室暂无患者')
    return
  }

  const data = generatePreviewData({
    shift: form.shift,
    toDoctorId: form.toDoctorId,
    patients: form.patients.map(p => ({
      id: p.id,
      bedNumber: p.bedNumber,
      name: p.name,
      age: p.age,
      gender: p.gender,
      diagnosis: p.diagnosis,
      condition: p.currentCondition,
      vitals: p.vitals,
      observationItems: p.observationItems
    })),
    stats: { ...stats }
  })
  previewData.value = data
  showPreviewDialog.value = true
}

const handleSubmit = async () => {
  // 新增：检查是否有排班
  if (!form.toDoctorId && noDutyStaffMsg.value) {
    ElMessage.warning(noDutyStaffMsg.value)
    return
  }

  if (!form.toDoctorId) {
    ElMessage.warning('请选择接班医生')
    return
  }

  if (form.patients.length === 0) {
    ElMessage.warning('当前科室暂无患者，无法发起交班')
    return
  }

  submitting.value = true
  try {
    const today = new Date().toISOString().slice(0, 10)
    
    const res = await createHandover({
      deptId: authStore.currentDepartmentId!,
      handoverDate: today,
      shift: form.shift,
      fromDoctorId: authStore.userInfo!.hisStaffId || authStore.userInfo!.id,
      toDoctorId: form.toDoctorId
    })
    
    if (res.code === 0 && res.data) {
      const newHandoverId = res.data.id
      console.log('[handleSubmit] newHandoverId:', newHandoverId)
      
      const patientsRes = await fetchHandoverPatients(newHandoverId)
      console.log('[handleSubmit] patientsRes:', patientsRes)
      if (patientsRes.code === 0 && patientsRes.data) {
        for (const hp of patientsRes.data) {
          const editedPatient = form.patients.find(p => p.visitId === hp.visitId)
          console.log('[handleSubmit] matching visitId:', hp.visitId, '-> found:', !!editedPatient, 'hp.id:', hp.id)
          if (editedPatient && (editedPatient.vitals || editedPatient.currentCondition || editedPatient.observationItems)) {
            console.log('[handleSubmit] updating patient:', newHandoverId, hp.id)
            try {
              await updateHandoverPatient(newHandoverId, hp.id, {
                vitals: editedPatient.vitals || '',
                currentCondition: editedPatient.currentCondition || '',
                observationItems: editedPatient.observationItems || ''
              })
            } catch (e) {
              console.error('更新患者详情失败:', e)
            }
          }
        }
      }
      
      ElMessage.success('交班发起成功')
      
      const cleanupOverlays = () => {
        const overlays = document.querySelectorAll('.el-overlay, .el-overlay-modal, .el-dialog__wrapper')
        overlays.forEach(el => {
          if (el.parentNode) {
            el.parentNode.removeChild(el)
          }
        })
        document.body.classList.remove('el-popup-parent--hidden')
        document.body.style.overflow = ''
      }
      
      cleanupOverlays()
      router.push('/handovers')
    } else {
      ElMessage.error(res.message || '交班发起失败')
    }
  } catch {
    ElMessage.error('交班发起失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loadSystemConfig()
  if (isEditMode.value && handoverId.value) {
    await loadExistingHandover(handoverId.value)
  } else {
    loadStats()
    loadDutyStaff()
    loadPatients()
  }
})

const loadExistingHandover = async (id: number) => {
  try {
    const res = await fetchHandoverById(id)
    if (res.code === 0 && res.data) {
      form.handoverNo = res.data.handoverNo
      form.shift = res.data.shift || '夜班'
      form.toDoctorId = res.data.toDoctorId
      
      const patientsRes = await fetchHandoverPatients(id)
      if (patientsRes.code === 0) {
        form.patients = patientsRes.data.map(p => ({
          id: p.id,
          bedNumber: p.bedNo || '',
          name: p.patientName,
          gender: p.gender,
          age: p.age,
          diagnosis: p.diagnosis,
          isCritical: false,
          vitals: p.vitals || '',
          currentCondition: p.currentCondition || '',
          observationItems: p.observationItems || '',
          filterReason: p.filterReason,
          visitId: p.visitId
        }))
      }
      
      loadStats()
      
      doctorList.value = [{
        id: res.data.toDoctorId!,
        name: res.data.toDoctorName || '接班医生'
      }]
    } else {
      ElMessage.error('加载交班数据失败')
      router.push('/handovers')
    }
  } catch (error) {
    ElMessage.error('加载交班数据失败')
    router.push('/handovers')
  }
}
</script>

<style>
.create-handover-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.page-header {
  height: 64px;
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 64px;
  z-index: 50;
  box-shadow: 0 2px 12px rgba(255, 179, 102, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left .back-btn {
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  color: white !important;
  border: 1px solid #3b82f6;
  background: #3b82f6;
  transition: all 0.2s ease;
}

.header-left .back-btn:hover,
.header-left .back-btn:focus,
.header-left .back-btn:active {
  background: #2563eb !important;
  border-color: #2563eb !important;
  color: white !important;
}

.page-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  flex-direction: row;
  white-space: nowrap;
}

.dept-badge {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.4);
  color: white;
  border-radius: 4px;
}

.handover-no-badge {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  background: rgba(255, 179, 102, 0.3);
  color: white;
  border-radius: 4px;
  margin-left: 8px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.page-content {
  padding: 24px;
  max-width: 1800px;
  margin: 0 auto;
}

.form-section {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-title .el-icon {
  color: var(--color-primary-DEFAULT);
}

.form-row {
  display: flex;
  gap: 40px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.required {
  color: #f56c6c;
}

.no-duty-tip {
  font-size: 12px;
  color: #e6a23c;
  margin-left: 8px;
}

.shift-radio {
  display: flex;
  gap: 12px;
}

.shift-radio .el-radio-button__inner {
  padding: 10px 24px;
  border-radius: 8px !important;
  border: 1px solid var(--border-color);
}

.shift-radio .el-radio-button__original-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  border-color: #ffb366;
}

.stats-section {
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border-radius: var(--radius-lg);
  padding: 16px 24px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 179, 102, 0.2);
}

.stats-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  align-items: center;
}

.sync-time {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-item .stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-item .stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-item.primary .stat-value {
  color: #ea580c;
}

.stat-item.warning .stat-value {
  color: #d97706;
}

.stat-item.danger .stat-label {
  color: #dc2626;
}

.patient-section {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header .section-title {
  margin-bottom: 0;
}

.patient-table {
  border-radius: var(--radius-md);
}

.patient-table .el-table__body tr {
  height: auto;
}

.patient-table .el-table__body td {
  height: auto;
  padding: 8px 0;
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

.vitals-display {
  font-size: 12px;
  line-height: 1.5;
}

.vitals-content {
  white-space: pre-line;
  color: #303133;
}

.vitals-empty {
  color: #909399;
  font-size: 12px;
}

.patient-name {
  font-weight: 500;
  color: var(--text-primary);
}

.patient-section .el-textarea__inner {
  border-radius: 6px;
  font-size: 13px;
}
</style>