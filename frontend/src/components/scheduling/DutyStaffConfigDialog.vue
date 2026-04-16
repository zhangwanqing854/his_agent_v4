<template>
  <el-dialog
    v-model="visible"
    title="值班人员配置"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="duty-staff-config">
      <div class="config-header">
        <el-button type="primary" @click="showAddDialog" :disabled="availableStaff.length === 0">
          添加值班人员
        </el-button>
        <el-button type="warning" @click="handleInitialize" v-if="dutyStaffList.length === 0">
          一键初始化
        </el-button>
        <el-button type="danger" @click="handleClear" v-if="dutyStaffList.length > 0">
          清空列表
        </el-button>
      </div>
      
      <div class="staff-list" v-if="dutyStaffList.length > 0">
        <div class="staff-item" v-for="(staff, index) in dutyStaffList" :key="staff.id">
          <el-button
            v-if="index > 0"
            type="primary"
            link
            size="small"
            @click="moveUp(index)"
          >
            ↑
          </el-button>
          <span class="staff-info">
            <span class="staff-name">{{ staff.staffName }}</span>
            <span class="staff-title">{{ staff.title }}</span>
          </span>
          <el-button
            v-if="index < dutyStaffList.length - 1"
            type="primary"
            link
            size="small"
            @click="moveDown(index)"
          >
            ↓
          </el-button>
          <span class="order-badge">{{ index + 1 }}</span>
          <el-button type="danger" link size="small" @click="handleRemove(staff.staffId)">
            删除
          </el-button>
        </div>
      </div>
      
      <el-empty v-else description="暂无值班人员，请添加或一键初始化" />
    </div>
    
    <el-dialog
      v-model="addDialogVisible"
      title="添加值班人员"
      width="500px"
      append-to-body
    >
      <el-checkbox-group v-model="selectedStaffIds">
        <div class="available-staff-list">
          <el-checkbox
            v-for="staff in availableStaff"
            :key="staff.id"
            :value="staff.id"
            :label="staff.id"
          >
            <span class="staff-name">{{ staff.name }}</span>
            <span class="staff-title">{{ staff.title }}</span>
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :disabled="selectedStaffIds.length === 0">
          添加（{{ selectedStaffIds.length }}人）
        </el-button>
      </template>
    </el-dialog>
    
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleSaveOrder" v-if="dutyStaffList.length > 0">
        保存排序
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchDutyStaff,
  addDutyStaff,
  removeDutyStaff,
  updateDutyStaffOrder,
  initializeDutyStaff,
  fetchSchedulingStaff,
  type DutyStaff,
  type Staff
} from '@/api/scheduling'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'updated'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dutyStaffList = ref<DutyStaff[]>([])
const allStaffList = ref<Staff[]>([])
const addDialogVisible = ref(false)
const selectedStaffIds = ref<number[]>([])

const availableStaff = computed(() => {
  const dutyStaffIds = dutyStaffList.value.map(s => s.staffId)
  return allStaffList.value.filter(s => !dutyStaffIds.includes(s.id))
})

const loadData = async () => {
  try {
    const dutyRes = await fetchDutyStaff()
    if (dutyRes.code === 0) {
      dutyStaffList.value = dutyRes.data
    }
    
    const staffRes = await fetchSchedulingStaff()
    if (staffRes.code === 0) {
      allStaffList.value = staffRes.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

watch(visible, (val) => {
  if (val) {
    loadData()
    selectedStaffIds.value = []
  }
})

const showAddDialog = () => {
  selectedStaffIds.value = []
  addDialogVisible.value = true
}

const handleAdd = async () => {
  if (selectedStaffIds.value.length === 0) return
  
  try {
    const res = await addDutyStaff(selectedStaffIds.value)
    if (res.code === 0) {
      ElMessage.success(`添加成功，共 ${res.data.length} 人`)
      dutyStaffList.value = res.data
      addDialogVisible.value = false
      emit('updated')
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleRemove = async (staffId: number) => {
  try {
    await ElMessageBox.confirm('确定要移除该值班人员吗？', '提示', {
      type: 'warning'
    })
    
    const res = await removeDutyStaff(staffId)
    if (res.code === 0) {
      ElMessage.success('移除成功')
      dutyStaffList.value = dutyStaffList.value.filter(s => s.staffId !== staffId)
      emit('updated')
    }
  } catch {
    // 用户取消
  }
}

const moveUp = (index: number) => {
  if (index <= 0) return
  const temp = dutyStaffList.value[index]
  dutyStaffList.value[index] = dutyStaffList.value[index - 1]
  dutyStaffList.value[index - 1] = temp
}

const moveDown = (index: number) => {
  if (index >= dutyStaffList.value.length - 1) return
  const temp = dutyStaffList.value[index]
  dutyStaffList.value[index] = dutyStaffList.value[index + 1]
  dutyStaffList.value[index + 1] = temp
}

const handleSaveOrder = async () => {
  const staffIds = dutyStaffList.value.map(s => s.staffId)
  
  try {
    const res = await updateDutyStaffOrder(staffIds)
    if (res.code === 0) {
      ElMessage.success('排序保存成功')
      dutyStaffList.value = res.data
      emit('updated')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleInitialize = async () => {
  try {
    await ElMessageBox.confirm(
      '一键初始化将导入所有科室工作人员为值班人员，是否继续？',
      '提示',
      { type: 'info' }
    )
    
    const res = await initializeDutyStaff()
    if (res.code === 0) {
      ElMessage.success(`初始化成功，共 ${res.data.length} 人`)
      dutyStaffList.value = res.data
      emit('updated')
    }
  } catch {
    // 用户取消
  }
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有值班人员吗？清空后排班功能将无法正常使用。',
      '警告',
      { type: 'warning' }
    )
    
    for (const staff of dutyStaffList.value) {
      await removeDutyStaff(staff.staffId)
    }
    
    ElMessage.success('清空成功')
    dutyStaffList.value = []
    emit('updated')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.duty-staff-config {
  min-height: 200px;
}

.config-header {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}

.staff-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.staff-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.staff-info {
  flex: 1;
  display: flex;
  gap: 8px;
}

.staff-name {
  font-size: 14px;
  font-weight: 500;
}

.staff-title {
  font-size: 12px;
  color: #909399;
}

.order-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: var(--color-primary-DEFAULT);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
}

.available-staff-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}
</style>