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
        <el-button @click="showDutyStaffConfig = true">
          <el-icon><Setting /></el-icon>
          配置值班人员
        </el-button>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建排班
        </el-button>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="filter-bar">
        <div class="filter-left">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            @change="handleMonthChange"
          />
        </div>
        <div class="filter-right">
          <el-button v-if="currentScheduling" type="primary" @click="handlePrint">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </div>
      </div>

      <div v-loading="loading" class="scheduling-content">
        <div v-if="schedulingList.length === 0" class="empty-state">
          <el-empty description="暂无排班数据，请先创建排班计划">
            <el-button type="primary" @click="handleCreate">创建排班</el-button>
          </el-empty>
        </div>

        <div v-else class="scheduling-grid">
          <div v-for="item in schedulingList" :key="item.id" class="scheduling-item">
            <div class="item-header">
              <span class="item-month">{{ item.yearMonth }}</span>
              <el-tag :type="item.status === 'published' ? 'success' : 'info'" size="small">
                {{ item.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </div>
            <div class="item-actions">
              <el-button type="primary" link size="small" @click="handleView(item)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button 
                v-if="item.status === 'draft'" 
                type="success" 
                link 
                size="small" 
                @click="handlePublish(item)"
              >
                <el-icon><Upload /></el-icon>
                发布
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(item)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="currentScheduling && !isEmptySchedule && currentSchedulingDetails.length > 0" class="card-display">
          <SchedulingCard
            :year-month="currentScheduling.yearMonth"
            :department-name="currentScheduling.departmentName"
            :details="currentSchedulingDetails"
            :editable="currentScheduling.status === 'draft'"
            @day-click="handleDayClick"
          />
        </div>

        <div v-if="currentScheduling && isEmptySchedule" class="empty-scheduling">
          <el-empty description="空白排班，点击快速生成或手动安排">
            <el-button type="primary" @click="handleQuickGenerate">
              <el-icon><MagicStick /></el-icon>
              快速生成
            </el-button>
          </el-empty>
        </div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Calendar, Plus, Printer, View, Upload, Delete, MagicStick, Setting } from '@element-plus/icons-vue'
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

const router = useRouter()
const loading = ref(false)
const selectedMonth = ref('')
const schedulingList = ref<SchedulingListItem[]>([])
const currentScheduling = ref<Scheduling | null>(null)
const currentSchedulingDetails = ref<SchedulingDetail[]>([])
const staffList = ref<Staff[]>([])

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

const loadData = async () => {
  loading.value = true
  try {
    const res = await fetchSchedulingList(selectedMonth.value)
    if (res.code === 0) {
      schedulingList.value = res.data
      if (res.data.length > 0 && !currentScheduling.value) {
        await loadDetail(res.data[0].id)
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
  loadData()
  loadStaffList()
})

const handleMonthChange = () => {
  currentScheduling.value = null
  loadData()
}

const handleCreate = () => {
  const now = new Date()
  createForm.yearMonth = `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}`
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
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleView = async (item: SchedulingListItem) => {
  await loadDetail(item.id)
}

const handlePublish = async (item: SchedulingListItem) => {
  try {
    await ElMessageBox.confirm('确定发布该排班计划？发布后所有科室人员可查看', '发布确认')
    const res = await updateSchedulingStatus(item.id, 'published')
    if (res.code === 0) {
      ElMessage.success('发布成功')
      item.status = 'published'
      if (currentScheduling.value?.id === item.id) {
        currentScheduling.value.status = 'published'
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

const handleDelete = async (item: SchedulingListItem) => {
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

.content-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.scheduling-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
}

.scheduling-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
  min-width: 200px;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-month {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.card-display {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>