<template>
  <el-dialog
    v-model="visible"
    title="快速生成排班"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="人员排序">
        <el-alert
          v-if="orderedStaff.length === 0"
          type="warning"
          :closable="false"
          style="margin-bottom: 12px"
        >
          暂无值班人员，请先配置值班人员
        </el-alert>
        <div class="staff-order-list" v-if="orderedStaff.length > 0">
          <div 
            v-for="(staff, index) in orderedStaff" 
            :key="staff.id"
            class="staff-item"
          >
            <el-button 
              v-if="index > 0"
              type="primary" 
              link 
              size="small"
              @click="moveUp(index)"
            >
              ↑
            </el-button>
            <span class="staff-name">{{ staff.name }}</span>
            <el-button 
              v-if="index < orderedStaff.length - 1"
              type="primary" 
              link 
              size="small"
              @click="moveDown(index)"
            >
              ↓
            </el-button>
            <span class="order-badge">{{ index + 1 }}</span>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="开始日期">
        <el-date-picker
          v-model="form.startDate"
          type="date"
          placeholder="选择开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="结束日期">
        <el-date-picker
          v-model="form.endDate"
          type="date"
          placeholder="选择结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item v-if="isFirstScheduling" label="起始位置">
        <el-radio-group v-model="form.startPosition">
          <el-radio 
            v-for="(staff, index) in orderedStaff" 
            :key="staff.id"
            :value="index"
          >
            从第{{ index + 1 }}位开始（{{ staff.name }}）
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-else label="接续提示">
        <el-alert type="info" :closable="false">
          <template #title>
            自动接续上月排班
          </template>
          上月月末排到 {{ lastMonthStaffName || '（无数据）' }}，
          本月将从 {{ nextStaffName }} 开始
        </el-alert>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleGenerate" :loading="loading">
        生成
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchSchedulingStaff,
  fetchSchedulingConfig,
  updateSchedulingConfig,
  autoGenerateScheduling,
  type Staff,
  type SchedulingConfig,
  type SchedulingDetail
} from '@/api/scheduling'

interface Props {
  modelValue: boolean
  schedulingId: number
  yearMonth: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'generated', details: SchedulingDetail[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const staffList = ref<Staff[]>([])
const config = ref<SchedulingConfig | null>(null)
const orderedStaff = ref<Staff[]>([])

const form = reactive({
  startDate: '',
  endDate: '',
  startPosition: 0
})

const isFirstScheduling = computed(() => config.value?.lastPosition === null)

const lastMonthStaffName = computed(() => {
  if (!config.value || config.value.lastPosition === null) return null
  const staffId = config.value.staffOrder[config.value.lastPosition]
  return staffList.value.find(s => s.id === staffId)?.name || null
})

const nextStaffName = computed(() => {
  if (!config.value || config.value.lastPosition === null) return null
  const nextPosition = (config.value.lastPosition + 1) % config.value.staffOrder.length
  const staffId = config.value.staffOrder[nextPosition]
  return staffList.value.find(s => s.id === staffId)?.name || null
})

const loadData = async () => {
  try {
    const staffRes = await fetchSchedulingStaff()
    if (staffRes.code === 0) {
      staffList.value = staffRes.data
    }

    const configRes = await fetchSchedulingConfig()
    if (configRes.code === 0) {
      config.value = configRes.data
      
      if (configRes.data.staffOrder.length > 0) {
        orderedStaff.value = configRes.data.staffOrder
          .map(id => staffList.value.find(s => s.id === id))
          .filter(Boolean) as Staff[]
      } else {
        orderedStaff.value = [...staffList.value]
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

const initForm = () => {
  const [year, month] = props.yearMonth.split('-')
  const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate()
  
  form.startDate = `${props.yearMonth}-01`
  form.endDate = `${props.yearMonth}-${daysInMonth.toString().padStart(2, '0')}`
  form.startPosition = 0
  
  if (!isFirstScheduling.value && config.value) {
    form.startPosition = (config.value.lastPosition! + 1) % config.value.staffOrder.length
  }
}

onMounted(() => {
  loadData()
})

watch(visible, (val) => {
  if (val) {
    initForm()
  }
})

const moveUp = (index: number) => {
  if (index <= 0) return
  const temp = orderedStaff.value[index]
  orderedStaff.value[index] = orderedStaff.value[index - 1]
  orderedStaff.value[index - 1] = temp
}

const moveDown = (index: number) => {
  if (index >= orderedStaff.value.length - 1) return
  const temp = orderedStaff.value[index]
  orderedStaff.value[index] = orderedStaff.value[index + 1]
  orderedStaff.value[index + 1] = temp
}

const handleGenerate = async () => {
  if (!form.startDate || !form.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }

  if (orderedStaff.value.length === 0) {
    ElMessage.warning('请至少选择一位排班人员')
    return
  }

  loading.value = true

  try {
    const staffOrder = orderedStaff.value.map(s => s.id)
    
    await updateSchedulingConfig(staffOrder)
    
    const res = await autoGenerateScheduling(props.schedulingId, {
      startDate: form.startDate,
      endDate: form.endDate,
      startPosition: form.startPosition,
      staffOrder
    })

    if (res.code === 0) {
      ElMessage.success('生成成功')
      emit('generated', res.data)
      visible.value = false
    }
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.staff-order-list {
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

.staff-name {
  flex: 1;
  font-size: 14px;
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
</style>