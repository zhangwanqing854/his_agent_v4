<template>
  <div class="scheduling-container">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </div>
      <h1 class="page-title">
        <el-icon class="title-icon"><Calendar /></el-icon>
        科室排班
      </h1>
      <div class="header-right">
        <el-button v-if="isCurrentDepartment" @click="showDutyStaffConfig = true">
          <el-icon><Setting /></el-icon>
          配置值班人员
        </el-button>
        <el-button v-if="isCurrentDepartment" type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建排班
        </el-button>
        <el-button v-if="currentScheduling" type="primary" @click="handlePrint">
          <el-icon><Printer /></el-icon>
          打印
        </el-button>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 月份导航区域 -->
      <div class="month-navigation">
        <!-- 科室选择器 -->
        <div class="nav-left">
          <el-select 
            v-model="selectedDepartmentId" 
            placeholder="选择科室" 
            class="department-selector"
            filterable
          >
            <el-option
              v-for="dept in authStore.userDepartments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </div>
        
        <!-- 月份导航 - 居中 -->
        <div class="nav-center">
          <el-button class="nav-btn" @click="handlePrevMonth">
            <el-icon><ArrowLeft /></el-icon>
            上一月
          </el-button>
          <div class="current-month">{{ formatCurrentMonth }}</div>
          <el-button class="nav-btn" @click="handleNextMonth">
            下一月
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        
        <!-- 排班状态和操作按钮 -->
        <div class="nav-right">
          <div v-if="currentScheduling && isCurrentDepartment" class="scheduling-status-actions">
            <el-tag :type="currentScheduling.status === 'published' ? 'success' : 'info'" size="large">
              {{ currentScheduling.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
            <el-button 
              v-if="currentScheduling.status === 'draft'" 
              type="success" 
              @click="handlePublish(currentScheduling)"
            >
              <el-icon><Upload /></el-icon>
              发布
            </el-button>
            <el-button type="danger" @click="handleDelete(currentScheduling)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 排班内容区域 -->
      <div v-loading="loading" class="scheduling-content">
        <!-- 无排班时显示创建入口 -->
        <div v-if="!currentScheduling" class="empty-state">
          <el-empty :description="schedulingList.length === 0 ? '暂无排班数据，请先创建排班计划' : '当前月份无排班'">
            <el-button type="primary" @click="handleCreateForCurrentMonth">
              <el-icon><Plus /></el-icon>
              创建{{ formatCurrentMonth }}排班
            </el-button>
          </el-empty>
        </div>

        <!-- 有排班时显示排班内容 -->
        <template v-else>
          <!-- 空白排班提示 -->
          <div v-if="isEmptySchedule" class="empty-scheduling">
            <el-empty description="空白排班，点击快速生成或手动安排">
              <el-button type="primary" @click="handleQuickGenerate">
                <el-icon><MagicStick /></el-icon>
                快速生成
              </el-button>
            </el-empty>
          </div>

          <!-- 排班日历 -->
          <div v-else class="calendar-display">
            <SchedulingCard
              :year-month="currentScheduling.yearMonth"
              :department-name="currentScheduling.departmentName"
              :details="currentSchedulingDetails"
              :editable="currentScheduling.status === 'draft' && isCurrentDepartment"
              :current-staff-id="authStore.hisStaffId"
              @day-click="handleDayClick"
            />
          </div>
        </template>
      </div>
    </div>

    <el-dialog v-model="createDialogVisible" title="创建排班计划" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="排班月份" required>
          <el-date-picker
            v-model="createForm.yearMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑排班" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="值班日期">
          <span>{{ editForm.dutyDate }}</span>
        </el-form-item>
        <el-form-item label="值班人员">
          <el-select v-model="editForm.staffId" placeholder="选择值班人员" clearable filterable>
            <el-option
              v-for="staff in staffList"
              :key="staff.id"
              :label="staff.name"
              :value="staff.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>

    <Teleport to="body">
      <SchedulingPrint
        v-if="printDialogVisible"
        :list="schedulingList"
        :default-month="currentScheduling?.yearMonth"
        @close="printDialogVisible = false"
      />
    </Teleport>

    <SchedulingQuickGenerateDialog
      v-model="quickGenerateDialogVisible"
      :scheduling-id="currentScheduling?.id || 0"
      :year-month="currentScheduling?.yearMonth || ''"
      @generated="handleGenerated"
    />

    <DutyStaffConfigDialog
      v-model="showDutyStaffConfig"
      @updated="handleDutyStaffUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Calendar, Plus, Printer, Upload, Delete, MagicStick, Setting } from '@element-plus/icons-vue'
import SchedulingCard from '@/components/scheduling/SchedulingCard.vue'
import SchedulingPrint from '@/components/scheduling/SchedulingPrint.vue'
import SchedulingQuickGenerateDialog from '@/components/scheduling/SchedulingQuickGenerateDialog.vue'
import DutyStaffConfigDialog from '@/components/scheduling/DutyStaffConfigDialog.vue'
import {
  fetchSchedulingList,
  fetchSchedulingDetail,
  createScheduling,
  updateSchedulingStatus,
  deleteScheduling,
  updateSchedulingDetails,
  fetchSchedulingStaff,
  type SchedulingListItem,
  type Scheduling,
  type SchedulingDetail,
  type Staff
} from '@/api/scheduling'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const schedulingList = ref<SchedulingListItem[]>([])
const currentScheduling = ref<Scheduling | null>(null)
const currentSchedulingDetails = ref<SchedulingDetail[]>([])
const staffList = ref<Staff[]>([])

// 当前选中的月份
const currentMonth = ref('')

// 选中的科室ID（用于查看不同科室的排班）
const selectedDepartmentId = ref<number | null>(null)

// 判断是否为当前用户所在的科室
const isCurrentDepartment = computed(() => 
  selectedDepartmentId.value === authStore.currentDepartmentId
)

const formatCurrentMonth = computed(() => {
  if (!currentMonth.value) return ''
  const [year, month] = currentMonth.value.split('-')
  return `${year}年${parseInt(month)}月`
})

const isEmptySchedule = computed(() => 
  currentSchedulingDetails.value.length > 0 && 
  currentSchedulingDetails.value.every(d => d.staffId === null)
)

const createDialogVisible = ref(false)
const createForm = reactive({ yearMonth: '' })

const editDialogVisible = ref(false)
const editForm = reactive({
  dutyDate: '',
  staffId: null as number | null,
  remark: ''
})

const printDialogVisible = ref(false)
const quickGenerateDialogVisible = ref(false)
const showDutyStaffConfig = ref(false)

// 初始化当前月份
const initCurrentMonth = () => {
  const now = new Date()
  currentMonth.value = `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}`
}

// 初始化选中的科室
const initSelectedDepartment = () => {
  selectedDepartmentId.value = authStore.currentDepartmentId
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await fetchSchedulingList()
    if (res.code === 0) {
      schedulingList.value = res.data
      // 查找当前月份的排班
      const currentMonthScheduling = res.data.find(s => s.yearMonth === currentMonth.value)
      if (currentMonthScheduling) {
        await loadDetail(currentMonthScheduling.id)
      } else {
        currentScheduling.value = null
        currentSchedulingDetails.value = []
      }
    }
  } catch (error) {
    console.error('加载排班列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDetail = async (id: number) => {
  try {
    const res = await fetchSchedulingDetail(id)
    if (res.code === 0) {
      currentScheduling.value = res.data
      currentSchedulingDetails.value = res.data.details
    }
  } catch (error) {
    console.error('加载排班详情失败:', error)
  }
}

const loadStaffList = async () => {
  try {
    const res = await fetchSchedulingStaff()
    if (res.code === 0) {
      staffList.value = res.data
    }
  } catch (error) {
    console.error('加载人员列表失败:', error)
  }
}

onMounted(() => {
  initCurrentMonth()
  initSelectedDepartment()
  loadData()
  loadStaffList()
})

// 监听月份变化
watch(currentMonth, () => {
  loadData()
})

// 监听科室变化
watch(selectedDepartmentId, () => {
  loadData()
})

const handlePrevMonth = () => {
  const [year, month] = currentMonth.value.split('-').map(Number)
  const prevMonth = month === 1 ? 12 : month - 1
  const prevYear = month === 1 ? year - 1 : year
  currentMonth.value = `${prevYear}-${prevMonth.toString().padStart(2, '0')}`
}

const handleNextMonth = () => {
  const [year, month] = currentMonth.value.split('-').map(Number)
  const nextMonth = month === 12 ? 1 : month + 1
  const nextYear = month === 12 ? year + 1 : year
  currentMonth.value = `${nextYear}-${nextMonth.toString().padStart(2, '0')}`
}

const handleMonthSelect = (month: string) => {
  currentMonth.value = month
}

const handleCreate = () => {
  createForm.yearMonth = currentMonth.value
  createDialogVisible.value = true
}

const handleCreateForCurrentMonth = () => {
  createForm.yearMonth = currentMonth.value
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createForm.yearMonth) {
    ElMessage.warning('请选择排班月份')
    return
  }

  const exists = schedulingList.value.find(s => s.yearMonth === createForm.yearMonth)
  if (exists) {
    ElMessage.warning('该月份排班已存在')
    return
  }

  try {
    const res = await createScheduling(createForm.yearMonth)
    if (res.code === 0) {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      schedulingList.value.push(res.data)
      await loadDetail(res.data.id)
    }
  } catch (error: any) {
    if (error?.message?.includes('当前科室为空')) {
      ElMessage.error('当前科室为空，请退出重新登录')
    } else if (error?.message?.includes('未授权')) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('创建失败')
    }
  }
}

const handlePublish = async (item: Scheduling | SchedulingListItem) => {
  try {
    await ElMessageBox.confirm('确定发布该排班计划？发布后所有科室人员可查看', '发布确认')
    const res = await updateSchedulingStatus(item.id, 'published')
    if (res.code === 0) {
      ElMessage.success('发布成功')
      if (currentScheduling.value?.id === item.id) {
        currentScheduling.value.status = 'published'
      }
      // 更新列表中的状态
      const listItem = schedulingList.value.find(s => s.id === item.id)
      if (listItem) listItem.status = 'published'
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

const handleDelete = async (item: Scheduling | SchedulingListItem) => {
  try {
    await ElMessageBox.confirm('确定删除该排班计划？删除后无法恢复', '删除确认', {
      type: 'warning'
    })
    const res = await deleteScheduling(item.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      schedulingList.value = schedulingList.value.filter(s => s.id !== item.id)
      if (currentScheduling.value?.id === item.id) {
        currentScheduling.value = null
        currentSchedulingDetails.value = []
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDayClick = (day: { date: string; staffId: number | null; staffName: string | null }) => {
  editForm.dutyDate = day.date
  editForm.staffId = day.staffId
  editForm.remark = ''
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!currentScheduling.value) return

  try {
    const details = currentSchedulingDetails.value.map(d => ({
      dutyDate: d.dutyDate,
      staffId: d.dutyDate === editForm.dutyDate ? editForm.staffId : d.staffId,
      remark: d.dutyDate === editForm.dutyDate ? editForm.remark : d.remark
    }))

    const res = await updateSchedulingDetails(currentScheduling.value.id, details)
    if (res.code === 0) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      
      const detailIndex = currentSchedulingDetails.value.findIndex(d => d.dutyDate === editForm.dutyDate)
      if (detailIndex > -1) {
        const staff = staffList.value.find(s => s.id === editForm.staffId)
        currentSchedulingDetails.value[detailIndex].staffId = editForm.staffId
        currentSchedulingDetails.value[detailIndex].staffName = staff?.name || null
        currentSchedulingDetails.value[detailIndex].remark = editForm.remark
      }
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handlePrint = () => {
  printDialogVisible.value = true
}

const handleQuickGenerate = () => {
  if (!currentScheduling.value) {
    ElMessage.warning('请先选择一个排班计划')
    return
  }
  quickGenerateDialogVisible.value = true
}

const handleGenerated = (details: SchedulingDetail[]) => {
  currentSchedulingDetails.value = details
}

const handleDutyStaffUpdated = async () => {
  const res = await fetchSchedulingStaff()
  if (res.code === 0) {
    staffList.value = res.data
  }
}
</script>

<style scoped>
.scheduling-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0 24px 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: var(--color-primary-DEFAULT);
}

.header-right {
  display: flex;
  gap: 8px;
}

.content-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

/* 月份导航样式 */
.month-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.nav-left {
  flex: 1;
  display: flex;
  justify-content: flex-start;
}

.nav-center {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
}

.nav-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.department-selector {
  width: 160px;
}

.current-month {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  min-width: 120px;
  text-align: center;
}

.nav-btn {
  min-width: 100px;
}

.scheduling-status-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.scheduling-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.empty-scheduling {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.calendar-display {
  padding: 0;
}
</style>
