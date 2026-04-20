<template>
  <div class="patient-management">
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>科室患者 <span class="dept-badge">{{ authStore.currentDepartmentName }}</span></h1>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="view-tabs">
      <el-tab-pane label="表格视图" name="table">
        <el-card class="filter-card">
          <el-form :inline="true" :model="filterForm">
            <el-form-item label="床号">
              <el-input v-model="filterForm.bedNumber" placeholder="床号" style="width: 100px" clearable />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="filterForm.name" placeholder="患者姓名" style="width: 120px" clearable />
            </el-form-item>
            <el-form-item label="护理等级">
              <el-select v-model="filterForm.nurseLevel" placeholder="全部" style="width: 100px" clearable>
                <el-option label="特级" value="特级" />
                <el-option label="一级" value="一级" />
                <el-option label="二级" value="二级" />
                <el-option label="三级" value="三级" />
              </el-select>
            </el-form-item>
            <el-form-item label="入院日期">
              <el-date-picker
                v-model="filterForm.admissionDateStart"
                type="date"
                placeholder="开始日期"
                style="width: 140px"
                value-format="YYYY-MM-DD"
              />
              <span style="margin: 0 8px">~</span>
              <el-date-picker
                v-model="filterForm.admissionDateEnd"
                type="date"
                placeholder="结束日期"
                style="width: 140px"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card">
          <el-table :data="paginatedPatients" stripe v-loading="loading">
            <el-table-column prop="bedNo" label="床号" width="80" />
            <el-table-column prop="patientNo" label="患者编码" width="120" />
            <el-table-column prop="patientName" label="姓名" width="100" />
            <el-table-column prop="nurseLevel" label="护理等级" width="100" />
            <el-table-column label="危重" width="60">
              <template #default="{ row }">
                <el-tag v-if="row.isCritical" type="danger" size="small">危重</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="入院时间" width="180">
              <template #default="{ row }">
                {{ formatAdmissionTime(row.admissionDatetime) }}
              </template>
            </el-table-column>
            <el-table-column prop="deptName" label="科室" min-width="150" />
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="卡片视图" name="card">
        <div v-if="paginatedPatients.length === 0" class="empty-card">
          <el-empty description="暂无患者数据">
            <el-button type="primary" size="small" @click="handleReset">清除筛选</el-button>
          </el-empty>
        </div>
        <div v-else class="card-grid" v-loading="loading">
          <div
            v-for="patient in paginatedPatients"
            :key="patient.id"
            class="patient-card"
            :class="{ critical: patient.isCritical }"
          >
            <div class="card-header">
              <span class="bed-number">{{ patient.bedNo }}</span>
              <span v-if="patient.isCritical" class="critical-star">★</span>
              <span class="nurse-level-tag">{{ patient.nurseLevel }}</span>
            </div>

            <div class="card-info">
              <div class="patient-no">{{ patient.patientNo }}</div>
              <div class="patient-name">{{ patient.patientName }}</div>
              <div class="meta-info">
                <span>入院: {{ formatAdmissionTime(patient.admissionDatetime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-drawer v-model="drawerVisible" title="患者详情" size="600px" direction="rtl">
      <div v-if="selectedPatient" class="patient-detail">
        <div class="detail-section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="床号">{{ selectedPatient.bedNumber }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ selectedPatient.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ selectedPatient.gender }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ selectedPatient.age }}岁</el-descriptions-item>
            <el-descriptions-item label="住院号">{{ selectedPatient.hospitalId }}</el-descriptions-item>
            <el-descriptions-item label="入院日期">{{ selectedPatient.admissionDate }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ selectedPatient.department }}</el-descriptions-item>
            <el-descriptions-item label="主治医生">{{ selectedPatient.attendingDoctor }}</el-descriptions-item>
            <el-descriptions-item label="诊断" :span="2">{{ selectedPatient.diagnosis }}</el-descriptions-item>
            <el-descriptions-item label="护理等级">{{ selectedPatient.nurseLevel }}</el-descriptions-item>
            <el-descriptions-item label="重点患者">
              <el-tag v-if="selectedPatient.isCritical" type="danger" size="small">是</el-tag>
              <span v-else>否</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">生命体征趋势</div>
          <el-tabs v-model="currentVitalMetric" type="card">
            <el-tab-pane label="体温" name="temperature">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="temperature" />
            </el-tab-pane>
            <el-tab-pane label="脉搏" name="pulse">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="pulse" />
            </el-tab-pane>
            <el-tab-pane label="血压" name="bloodPressure">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="bloodPressure" />
            </el-tab-pane>
            <el-tab-pane label="血氧" name="oxygen">
              <VitalTrendChart :vitalsHistory="selectedPatient.vitalsHistory" metric="oxygen" />
            </el-tab-pane>
          </el-tabs>
          <div class="latest-vitals">
            最新测量: {{ selectedPatient.vitalsHistory[selectedPatient.vitalsHistory.length - 1]?.time }}
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">风险评估</div>
          <div class="risk-assessment">
            <div class="risk-item">
              <span class="risk-name">MEWS评分</span>
              <el-tag :type="getMewsTagType(selectedPatient.mewsScore)" size="large">
                {{ selectedPatient.mewsScore }}
              </el-tag>
              <span class="risk-desc">
                {{ selectedPatient.mewsScore >= 3 ? '高风险，需立即干预' : 
                   selectedPatient.mewsScore >= 1 ? '中风险，需关注' : '低风险' }}
              </span>
            </div>
            <div class="risk-item">
              <span class="risk-name">Braden评分</span>
              <el-tag :type="getBradenTagType(selectedPatient.bradenScore)" size="large">
                {{ selectedPatient.bradenScore }}
              </el-tag>
              <span class="risk-desc">
                {{ selectedPatient.bradenScore < 14 ? '高风险压疮' : 
                   selectedPatient.bradenScore < 16 ? '中风险压疮' : '低风险压疮' }}
              </span>
            </div>
            <div class="risk-item">
              <span class="risk-name">跌倒风险</span>
              <span class="risk-value">{{ selectedPatient.fallRisk }}分</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">过敏史/病史</div>
          <div class="history-section">
            <div class="history-item">
              <span class="history-label">过敏药物:</span>
              <el-tag v-for="allergy in selectedPatient.allergies" :key="allergy" type="warning" class="history-tag">
                {{ allergy }}
              </el-tag>
              <span v-if="!selectedPatient.allergies.length">无</span>
            </div>
            <div class="history-item">
              <span class="history-label">既往病史:</span>
              <span v-for="history in selectedPatient.medicalHistory" :key="history" class="history-text">
                {{ history }}
              </span>
              <span v-if="!selectedPatient.medicalHistory.length">无</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">护理信息</div>
          <div class="nursing-notes">{{ selectedPatient.nursingNotes || '无特殊护理事项' }}</div>
        </div>

        <div class="detail-section">
          <div class="section-title">交班记录</div>
          <el-timeline>
            <el-timeline-item
              v-for="record in selectedPatient.handoverRecords"
              :key="record.id"
              :timestamp="record.time"
              placement="top"
            >
              <div class="handover-item">
                {{ record.fromDoctor }} → {{ record.toDoctor }}
                <div class="handover-summary">{{ record.summary }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-button link type="primary">查看完整交班记录</el-button>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" :disabled="!selectedPatient" @click="handleHandover(selectedPatient)">交班</el-button>
        <el-button @click="drawerVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import VitalTrendChart from '@/components/VitalTrendChart.vue'
import { fetchInpatients, type InpatientDto } from '@/api/inpatient'

import type { Patient } from '@/types/patient'
const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref<'table' | 'card'>('table')
const drawerVisible = ref(false)
const selectedPatient = ref<InpatientDto | null>(null)
const currentVitalMetric = ref<'temperature' | 'pulse' | 'bloodPressure' | 'oxygen'>('temperature')

const filterForm = reactive({
  bedNumber: '',
  name: '',
  nurseLevel: '',
  admissionDateStart: '',
  admissionDateEnd: ''
})

const patientList = ref<InpatientDto[]>([])
const loading = ref(false)

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const filteredPatients = computed(() => {
  let result = [...patientList.value]
  
  if (filterForm.bedNumber) {
    result = result.filter(p => p.bedNo && p.bedNo.includes(filterForm.bedNumber))
  }
  if (filterForm.name) {
    result = result.filter(p => p.patientName && p.patientName.includes(filterForm.name))
  }
  if (filterForm.nurseLevel) {
    result = result.filter(p => p.nurseLevel === filterForm.nurseLevel)
  }
  if (filterForm.admissionDateStart) {
    result = result.filter(p => p.admissionDatetime && p.admissionDatetime >= filterForm.admissionDateStart)
  }
  if (filterForm.admissionDateEnd) {
    result = result.filter(p => p.admissionDatetime && p.admissionDatetime <= filterForm.admissionDateEnd + 'T23:59:59')
  }
  
  return result
})

const paginatedPatients = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  pagination.total = filteredPatients.value.length
  return filteredPatients.value.slice(start, end)
})

const fetchPatients = async () => {
  loading.value = true
  try {
    const deptId = authStore.currentDepartmentId
    if (!deptId) return
    const res = await fetchInpatients(deptId)
    if (res.code === 0 && res.data) {
      patientList.value = res.data
    } else {
      patientList.value = []
    }
  } catch {
    ElMessage.error('加载患者列表失败')
    patientList.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
}

const handleReset = () => {
  filterForm.bedNumber = ''
  filterForm.name = ''
  filterForm.nurseLevel = ''
  filterForm.admissionDateStart = ''
  filterForm.admissionDateEnd = ''
  pagination.page = 1
}

const handleView = (patient: InpatientDto) => {
  ElMessage.info(`查看患者: ${patient.patientName} (${patient.bedNo})`)
}

const handleHandover = (patient: InpatientDto) => {
  router.push(`/handovers/create?patientId=${patient.id}`)
}

const formatAdmissionTime = (datetime: string) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 16)
}

const getMewsTagType = (score: number) => {
  if (score >= 3) return 'danger'
  if (score >= 1) return 'warning'
  return 'success'
}

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

<style>
.patient-management {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  transition: all 0.2s ease;
}

.back-btn:hover {
  color: var(--color-primary-DEFAULT);
  border-color: var(--color-primary-DEFAULT);
  background: rgba(255, 179, 102, 0.05);
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
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
  background: rgba(255, 179, 102, 0.15);
  color: var(--color-primary-DEFAULT);
  border-radius: 4px;
}

.view-tabs {
  margin-top: 16px;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: var(--radius-lg);
}

.table-card {
  margin-bottom: 16px;
  border-radius: var(--radius-lg);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.risk-scores {
  display: flex;
  gap: 8px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 1400px) {
  .card-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 1000px) {
  .card-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .card-grid { grid-template-columns: 1fr; }
}

.empty-card {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.patient-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
  transition: all 0.2s ease;
}

.patient-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.patient-card.critical {
  border-color: #F56C6C;
  background: rgba(245, 108, 108, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.bed-number {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.critical-star {
  color: #F56C6C;
  font-size: 16px;
}

.nurse-level-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: rgba(255, 179, 102, 0.15);
  color: var(--color-primary-DEFAULT);
  border-radius: 4px;
}

.card-info {
  margin-bottom: 12px;
}

.name-gender-age {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.diagnosis {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.card-vitals {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.vital-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.vital-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.vital-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.vital-value.abnormal {
  color: #F56C6C;
}

.card-risks {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  justify-content: center;
}

.patient-detail {
  padding: 0 16px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.latest-vitals {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.risk-assessment {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.risk-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.risk-name {
  font-size: 14px;
  color: var(--text-secondary);
  width: 100px;
}

.risk-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.risk-value {
  font-size: 14px;
  font-weight: 500;
}

.history-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.history-label {
  font-size: 14px;
  color: var(--text-secondary);
  width: 80px;
}

.history-tag {
  margin-right: 4px;
}

.history-text {
  font-size: 14px;
  color: var(--text-primary);
  margin-right: 8px;
}

.nursing-notes {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.handover-item {
  font-size: 14px;
}

.handover-summary {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>