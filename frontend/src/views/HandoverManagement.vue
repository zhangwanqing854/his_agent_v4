<template>
  <div class="handover-management">
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>交班管理 <span class="dept-badge">{{ authStore.currentDepartmentName }}</span></h1>
      </div>
      <el-button type="primary" class="btn-primary-gradient" @click="router.push('/handovers/create')">
        <el-icon><Plus /></el-icon>
        发起交班
      </el-button>
    </div>

    <el-card class="table-card">
      <el-tabs v-model="activeTab" @tab-change="fetchHandovers">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待处理" name="pending" />
        <el-tab-pane label="交班中" name="transferring" />
        <el-tab-pane label="已完成" name="completed" />
      </el-tabs>

      <el-table :data="handoverList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="handoverNo" label="交班编号" width="120">
          <template #default="{ row }">
            {{ row.handoverNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室名称" width="120" />
        <el-table-column prop="patientCount" label="患者数" width="80" />
        <el-table-column prop="fromDoctorName" label="交班医生" width="100" />
        <el-table-column label="接班医生" width="100">
          <template #default="{ row }">
            {{ row.toDoctorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="handoverDate" label="交班日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
<el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="canEdit(row)"
              link
              type="primary"
              @click="handleEdit(row)"
            >
              修改
            </el-button>
            <el-button
              v-if="canSubmit(row)"
              link
              type="primary"
              @click="handleSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="canDelete(row)"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button
              v-if="canAccept(row)"
              link
              type="success"
              @click="handleAccept(row)"
            >
              接班
            </el-button>
            <el-button
              v-if="canReject(row)"
              link
              type="warning"
              @click="handleReject(row)"
            >
              退回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="showViewDialog"
      title="交班详情"
      width="800px"
      append-to-body
      :close-on-click-modal="false"
    >
      <div v-if="currentHandover" class="handover-detail">
        <!-- 科室交班摘要卡片 -->
        <el-card class="summary-card" shadow="never">
          <div class="summary-header">
            <h3 class="summary-title">科室交班摘要</h3>
            <el-tag :type="getStatusType(currentHandover.status)">
              {{ getStatusText(currentHandover.status) }}
            </el-tag>
          </div>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="科室名称">
              {{ currentHandover.departmentName }}
            </el-descriptions-item>
            <el-descriptions-item label="交班医生">
              {{ currentHandover.fromDoctorName }}
            </el-descriptions-item>
            <el-descriptions-item label="接班医生">
              {{ currentHandover.toDoctorName }}
            </el-descriptions-item>
            <el-descriptions-item label="交班日期">
              {{ currentHandover.handoverDate }}
            </el-descriptions-item>
            <el-descriptions-item label="班次">
              {{ currentHandover.shift || '白班' }}
            </el-descriptions-item>
            <el-descriptions-item label="患者总数">
              <span class="stat-value">{{ currentHandover.summary?.totalPatients || 0 }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="重点患者">
              <span class="stat-value critical">{{ currentHandover.summary?.criticalPatients || 0 }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="待办事项">
              <span class="stat-value">{{ currentHandover.summary?.pendingTodos || 0 }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 患者列表 -->
        <el-divider content-position="left">科室患者列表</el-divider>
        
        <el-table :data="currentHandover.patients || []" stripe style="width: 100%" v-loading="viewLoading">
          <el-table-column prop="bedNo" label="床号" width="80" />
          <el-table-column prop="patientName" label="姓名" width="100" />
          <el-table-column prop="diagnosis" label="诊断" min-width="150" />
          <el-table-column prop="filterReason" label="筛选原因" width="150" />
          <el-table-column label="目前情况" min-width="200">
            <template #default="{ row }">
              <div class="current-condition">{{ row.currentCondition || '-' }}</div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showViewDialog = false">关闭</el-button>
          <el-button
            v-if="currentHandover?.status === 'COMPLETED'"
            type="primary"
            @click="handlePrint"
          >
            打印交班报告
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 报告预览弹窗 -->
    <ReportPreview
      v-if="previewData"
      v-model="showPreviewDialog"
      :report-data="previewData"
      mode="view"
      @close="showPreviewDialog = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Printer } from '@element-plus/icons-vue'
import ReportPreview from '@/components/handover/ReportPreview.vue'
import { useAuthStore } from '@/stores/auth'
import { loadFullReportData, type PrintableReport } from '@/mocks/handoverPrintData'
import { fetchHandoverList, deleteHandover, submitHandover, acceptHandover, rejectHandover, fetchHandoverPatients, type HandoverDto, type HandoverPatientDto } from '@/api/handover'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const activeTab = ref('all')
const showViewDialog = ref(false)
const viewLoading = ref(false)
const currentHandover = ref<any>(null)
const allHandoverData = ref<HandoverDto[]>([])
const showPreviewDialog = ref(false)
const previewData = ref<PrintableReport | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const handoverList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return allHandoverData.value.slice(start, end)
})

const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    DRAFT: 'info',
    PENDING: 'warning',
    COMPLETED: 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待接班',
    COMPLETED: '已完成'
  }
  return texts[status] || status
}

const canAccept = (row: HandoverDto) => {
  return row.status === 'PENDING' && 
         row.toDoctorId === authStore.userInfo?.hisStaffId
}

const canReject = (row: HandoverDto) => {
  return row.status === 'PENDING' && 
         row.toDoctorId === authStore.userInfo?.hisStaffId
}

const canEdit = (row: HandoverDto) => {
  return row.status === 'DRAFT' && 
         row.fromDoctorId === authStore.userInfo?.hisStaffId
}

const canDelete = (row: HandoverDto) => {
  return row.status === 'DRAFT' && 
         row.fromDoctorId === authStore.userInfo?.hisStaffId
}

const canSubmit = (row: HandoverDto) => {
  return row.status === 'DRAFT' && 
         row.fromDoctorId === authStore.userInfo?.hisStaffId &&
         row.toDoctorId !== null
}

const allHandovers = [
  {
    id: 1,
    departmentId: 1,
    departmentName: '心内科',
    patientCount: 8,
    criticalPatientCount: 3,
    fromDoctorName: '张医生',
    toDoctorName: '李医生',
    handoverDate: '2026-03-20',
    shift: '白班',
    status: 'COMPLETED',
    summary: { totalPatients: 8, criticalPatients: 3, pendingTodos: 5 },
    patients: [
      { bedNumber: '1床', name: '张三', isCritical: true, diagnosis: '2型糖尿病、高血压3级', situation: '今日血糖波动较大，最高15.2mmol/L', background: '入院日期：2026-03-15' },
      { bedNumber: '2床', name: '李四', isCritical: true, diagnosis: '冠心病、心功能不全', situation: '今日胸闷症状加重', background: '入院日期：2026-03-10' },
      { bedNumber: '3床', name: '王五', isCritical: true, diagnosis: '急性心肌梗死', situation: '术后第2天，生命体征平稳', background: '入院日期：2026-03-18' },
      { bedNumber: '4床', name: '赵六', isCritical: false, diagnosis: '高血压2级', situation: '血压控制稳定', background: '入院日期：2026-03-12' },
      { bedNumber: '5床', name: '钱七', isCritical: false, diagnosis: '2型糖尿病', situation: '血糖控制良好', background: '入院日期：2026-03-14' }
    ],
    reportContent: {
      situation: '心内科今日共8名患者，重点患者3名。张三（1床）血糖波动较大，李四（2床）胸闷加重，王五（3床）术后平稳。',
      background: '张三：入院日期2026-03-15，今日血糖最高15.2mmol/L。李四：入院日期2026-03-10，冠心病史。',
      assessment: '张三：血糖控制不佳，MEWS评分2分。李四：病情稳定，MEWS评分1分。',
      recommendation: '张三：监测Q4H血糖，内分泌科会诊。李四：继续监测血压，完善心脏彩超。'
    },
    todos: [
      { content: '明日空腹血糖复查', dueTime: '2026-03-21 06:00', status: 'PENDING' },
      { content: '内分泌科会诊', dueTime: '2026-03-21 14:00', status: 'PENDING' }
    ]
  },
  {
    id: 2,
    departmentId: 1,
    departmentName: '心内科',
    patientCount: 8,
    criticalPatientCount: 2,
    fromDoctorName: '张医生',
    toDoctorName: '李医生',
    handoverDate: '2026-03-20',
    shift: '夜班',
    status: 'TRANSFERRING',
    summary: { totalPatients: 8, criticalPatients: 2, pendingTodos: 3 },
    patients: [
      { bedNumber: '1床', name: '张三', isCritical: true, diagnosis: '2型糖尿病', situation: '夜间血糖稳定', background: '入院日期：2026-03-15' },
      { bedNumber: '2床', name: '李四', isCritical: true, diagnosis: '冠心病', situation: '夜间无特殊主诉', background: '入院日期：2026-03-10' }
    ],
    reportContent: {
      situation: '心内科夜班交班，共8名患者，重点2名。',
      background: '夜间病情相对稳定。',
      assessment: '张三血糖控制可，李四病情平稳。',
      recommendation: '继续观察，按常规护理。'
    },
    todos: []
  },
  {
    id: 3,
    departmentId: 2,
    departmentName: '神经内科',
    patientCount: 5,
    criticalPatientCount: 4,
    fromDoctorName: '张医生',
    toDoctorName: '王医生',
    handoverDate: '2026-03-20',
    shift: '白班',
    status: 'PENDING',
    summary: { totalPatients: 5, criticalPatients: 4, pendingTodos: 7 },
    patients: [
      { bedNumber: '1床', name: '郑十一', isCritical: true, diagnosis: '脑梗死', situation: '今日肢体无力加重', background: '入院日期：2026-03-17' },
      { bedNumber: '2床', name: '王十二', isCritical: true, diagnosis: '脑出血', situation: '术后第1天，意识清楚', background: '入院日期：2026-03-19' },
      { bedNumber: '3床', name: '陈十三', isCritical: true, diagnosis: '癫痫持续状态', situation: '今日发作2次', background: '入院日期：2026-03-18' },
      { bedNumber: '4床', name: '林十四', isCritical: true, diagnosis: '重症肌无力', situation: '呼吸肌无力', background: '入院日期：2026-03-16' },
      { bedNumber: '5床', name: '黄十五', isCritical: false, diagnosis: '偏头痛', situation: '头痛缓解', background: '入院日期：2026-03-15' }
    ],
    reportContent: null,
    todos: []
  },
  {
    id: 4,
    departmentId: 3,
    departmentName: '呼吸科',
    patientCount: 3,
    criticalPatientCount: 2,
    fromDoctorName: '张医生',
    toDoctorName: '赵医生',
    handoverDate: '2026-03-20',
    shift: '白班',
    status: 'COMPLETED',
    summary: { totalPatients: 3, criticalPatients: 2, pendingTodos: 2 },
    patients: [
      { bedNumber: '1床', name: '孙八', isCritical: true, diagnosis: 'COPD急性加重', situation: '呼吸困难加重', background: '入院日期：2026-03-16' },
      { bedNumber: '2床', name: '周九', isCritical: true, diagnosis: '肺炎、呼吸衰竭', situation: '氧饱和度波动', background: '入院日期：2026-03-14' },
      { bedNumber: '3床', name: '吴十', isCritical: false, diagnosis: '支气管哮喘', situation: '病情稳定', background: '入院日期：2026-03-15' }
    ],
    reportContent: {
      situation: '呼吸科今日共3名患者，重点患者2名。',
      background: '孙八COPD急性加重，周九肺炎合并呼吸衰竭。',
      assessment: '孙八MEWS评分3分，周九MEWS评分2分。',
      recommendation: '孙八继续雾化治疗，周九调整氧疗方案。'
    },
    todos: []
  }
]

const fetchHandovers = async () => {
  loading.value = true
  pagination.page = 1
  try {
    const deptId = authStore.currentDepartmentId
    console.log('[HandoverManagement] fetchHandovers deptId:', deptId)
    
    if (!deptId) {
      console.log('[HandoverManagement] no deptId, skip')
      allHandoverData.value = []
      pagination.total = 0
      loading.value = false
      return
    }
    
    const res = await fetchHandoverList(deptId)
    console.log('[HandoverManagement] API response:', res)
    
    if (res && res.code === 0) {
      const data = Array.isArray(res.data) ? res.data : []
      
      let filtered = data
      
      if (activeTab.value !== 'all') {
        filtered = filtered.filter(item => item.status === activeTab.value.toUpperCase())
      }
      
      console.log('[HandoverManagement] filtered data:', filtered.length)
      allHandoverData.value = filtered
      pagination.total = filtered.length
    } else {
      console.error('[HandoverManagement] API error:', res)
      ElMessage.error(res?.message || '加载失败')
      allHandoverData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('[HandoverManagement] fetch error:', error)
    ElMessage.error('加载失败')
    allHandoverData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
}

const handleSizeChange = () => {
  pagination.page = 1
}

watch(() => authStore.currentDepartmentId, (newVal, oldVal) => {
  console.log('[HandoverManagement] deptId changed:', oldVal, '->', newVal)
  if (newVal && newVal !== oldVal) {
    fetchHandovers()
  }
})

watch(showViewDialog, (newVal, oldVal) => {
  console.log('[HandoverManagement] showViewDialog changed:', oldVal, '->', newVal)
})

onMounted(() => {
  console.log('[HandoverManagement] mounted, userInfo:', authStore.userInfo, 'deptId:', authStore.currentDepartmentId)
  
  if (authStore.userInfo && authStore.currentDepartmentId) {
    fetchHandovers()
  } else if (!authStore.userInfo) {
    authStore.fetchUserInfo().then(() => {
      console.log('[HandoverManagement] fetchUserInfo done, deptId:', authStore.currentDepartmentId)
      if (authStore.currentDepartmentId) {
        fetchHandovers()
      }
    }).catch(e => {
      console.error('[HandoverManagement] fetchUserInfo error:', e)
    })
  }
})

const forceCleanupAllOverlays = () => {
  const selectors = [
    '.el-overlay',
    '.el-overlay-modal',
    '.el-overlay-dialog',
    '.el-dialog__wrapper',
    '.el-message-box__wrapper'
  ]
  
  let removedCount = 0
  selectors.forEach(selector => {
    const elements = document.querySelectorAll(selector)
    elements.forEach(el => {
      if (el.parentNode) {
        el.parentNode.removeChild(el)
        removedCount++
      }
    })
  })
  
  document.body.classList.remove('el-popup-parent--hidden', 'el-popup-parent--lock')
  document.body.style.removeProperty('overflow')
  document.body.style.removeProperty('pointer-events')
  document.body.style.overflow = 'auto'
  document.body.style.pointerEvents = 'auto'
  
  console.log('[HandoverManagement] removed overlays:', removedCount)
}

const handleView = async (row: any) => {
  console.log('[HandoverManagement] handleView triggered for row:', row.id)
  console.log('[HandoverManagement] showViewDialog before:', showViewDialog.value)
  currentHandover.value = row
  showViewDialog.value = true
  console.log('[HandoverManagement] showViewDialog after:', showViewDialog.value)
  viewLoading.value = true
  
  try {
    const res = await fetchHandoverPatients(row.id)
    if (res.code === 0) {
      currentHandover.value = { ...row, patients: res.data || [] }
    } else {
      ElMessage.error(res.message || '加载患者数据失败')
    }
  } catch (error) {
    ElMessage.error('加载患者数据失败')
    console.error('[HandoverManagement] 加载患者数据失败:', error)
  } finally {
    viewLoading.value = false
  }
}

const handleEdit = (row: any) => {
  router.push(`/handovers/edit/${row.id}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除此交班记录吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteHandover(row.id)
      if (res.code === 0) {
        ElMessage.success('删除成功')
        fetchHandovers()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = (row: any) => {
  ElMessageBox.confirm('确定提交此交班记录吗？提交后等待接班医生确认接班。', '提交确认', {
    confirmButtonText: '确定提交',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const res = await submitHandover(row.id)
      if (res.code === 0) {
        ElMessage.success('提交成功，等待接班医生确认')
        fetchHandovers()
      } else {
        ElMessage.error(res.message || '提交失败')
      }
    } catch (error) {
      ElMessage.error('提交失败')
    }
  }).catch(() => {})
}

const handleAccept = (row: any) => {
  ElMessageBox.confirm('确认接班此交班记录？', '确认接班', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      const res = await acceptHandover(row.id)
      if (res.code === 0) {
        ElMessage.success('接班完成')
        fetchHandovers()
      } else {
        ElMessage.error(res.message || '接班失败')
      }
    } catch (error) {
      ElMessage.error('接班失败')
    }
  }).catch(() => {})
}

const handleReject = (row: any) => {
  ElMessageBox.prompt('请填写退回原因', '退回补充', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '请填写退回原因'
  }).then(async ({ value }) => {
    try {
      const res = await rejectHandover(row.id, value)
      if (res.code === 0) {
        ElMessage.warning(`已退回：${value}`)
        fetchHandovers()
      } else {
        ElMessage.error(res.message || '退回失败')
      }
    } catch (error) {
      ElMessage.error('退回失败')
    }
  }).catch(() => {})
}

const handlePrint = async () => {
  if (!currentHandover.value) return

  // 显示加载中
  const loadingInstance = ElMessage.info({
    message: '正在加载报告...',
    duration: 0
  })

  try {
    // 加载完整报告数据
    const data = await loadFullReportData(currentHandover.value.id)
    previewData.value = data
    showPreviewDialog.value = true
    showViewDialog.value = false
  } catch (error) {
    ElMessage.error('加载报告失败')
  } finally {
    // 关闭加载提示
    loadingInstance.close()
  }
}

onMounted(async () => {
  console.log('[HandoverManagement] onMounted start')
  
  setTimeout(() => {
    forceCleanupAllOverlays()
    console.log('[HandoverManagement] initial cleanup done')
  }, 500)
  
  if (!authStore.userInfo) {
    try {
      await authStore.fetchUserInfo()
    } catch (e) {
      console.error('[HandoverManagement] fetchUserInfo error:', e)
    }
  }
  if (authStore.currentDepartmentId) {
    fetchHandovers()
  }
})
</script>

<style>
.handover-management {
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

.page-header .el-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.page-header .el-button .el-icon {
  margin-right: 4px;
}

.table-card {
  border-radius: var(--radius-lg);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.handover-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.summary-card {
  margin-bottom: 20px;
  border-radius: var(--radius-md);
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.summary-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.stat-value {
  font-weight: 600;
  color: var(--text-primary);
}

.stat-value.critical {
  color: var(--color-danger-DEFAULT);
  font-weight: 700;
}

.report-content {
  padding: 16px 0;
}

.report-section {
  margin-bottom: 16px;
}

.report-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary-light);
  margin-bottom: 8px;
}

.report-section p {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  margin: 0;
  padding-left: 12px;
  border-left: 3px solid var(--color-primary-DEFAULT);
}

.sbar-preview {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sbar-line {
  font-size: 12px;
  color: var(--text-primary);
}

.sbar-line .label {
  font-weight: 600;
  color: var(--color-primary-DEFAULT);
  margin-right: 4px;
}

.text-placeholder {
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
