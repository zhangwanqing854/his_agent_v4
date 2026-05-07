<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1>欢迎使用医护智能交接班系统</h1>
      <p class="subtitle">
        <el-icon><User /></el-icon>
        {{ authStore.userName }} | 
        <span class="dept-name">{{ authStore.currentDepartmentName }}</span>
      </p>
    </div>

    <div class="stats-grid" v-if="authStore.hasDuty('PATIENT_MANAGEMENT') || authStore.hasDuty('HANDOVER_MANAGEMENT')">
      <el-card 
        v-if="authStore.hasDuty('PATIENT_MANAGEMENT')"
        class="stat-card clickable" 
        shadow="hover" 
        @click="router.push('/patients')"
      >
        <div class="stat-icon" style="background: linear-gradient(135deg, #ffb366, #ff9443)">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalPatients }}</div>
          <div class="stat-label">在院患者</div>
        </div>
      </el-card>

      <el-card 
        v-if="authStore.hasDuty('HANDOVER_MANAGEMENT')"
        class="stat-card clickable" 
        shadow="hover" 
        @click="router.push('/handovers?tab=completed')"
      >
        <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #529b2e)">
          <el-icon :size="32"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.completedHandovers }}</div>
          <div class="stat-label">已完成交班</div>
        </div>
      </el-card>

      <el-card 
        v-if="authStore.hasDuty('HANDOVER_MANAGEMENT')"
        class="stat-card clickable" 
        shadow="hover" 
        @click="router.push('/handovers?tab=pending')"
      >
        <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #d4880b)">
          <el-icon :size="32"><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingHandovers }}</div>
          <div class="stat-label">待处理交班</div>
        </div>
      </el-card>
    </div>

    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="actions-grid">
        <el-card 
          v-for="action in visibleActions" 
          :key="action.path"
          class="action-card" 
          shadow="hover" 
          @click="router.push(action.path)"
        >
          <el-icon :size="40" :color="action.color"><component :is="action.icon" /></el-icon>
          <div class="action-label">{{ action.label }}</div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchDashboardStats, type DashboardStatsDto } from '@/api/dashboardStats'
import { User, Document, CircleCheck, Bell, DataAnalysis, UserFilled, Key, Setting, Avatar, OfficeBuilding, Calendar, Connection, List } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const stats = ref<DashboardStatsDto>({
  totalPatients: 0,
  completedHandovers: 0,
  pendingHandovers: 0
})

const loading = ref(false)

const ACTION_CONFIG = [
  { path: '/handovers/create', label: '发起交班', dutyCode: 'HANDOVER_MANAGEMENT', icon: Document, color: '#ffb366' },
  { path: '/patients', label: '科室患者', dutyCode: 'PATIENT_MANAGEMENT', icon: User, color: '#67c23a' },
  { path: '/statistics', label: '统计分析', dutyCode: 'STATISTICS_MANAGEMENT', icon: DataAnalysis, color: '#e6a23c' },
  { path: '/todos', label: '待办事项', dutyCode: 'TODO_MANAGEMENT', icon: List, color: '#409eff' },
  { path: '/users', label: '用户管理', dutyCode: 'USER_MANAGEMENT', icon: UserFilled, color: '#9c27b0' },
  { path: '/roles', label: '角色权限', dutyCode: 'ROLE_MANAGEMENT', icon: Key, color: '#ff5722' },
  { path: '/his-staff', label: '人员管理', dutyCode: 'STAFF_MANAGEMENT', icon: Avatar, color: '#00bcd4' },
  { path: '/departments', label: '科室管理', dutyCode: 'DEPARTMENT_MANAGEMENT', icon: OfficeBuilding, color: '#9c27b0' },
  { path: '/scheduling', label: '科室排班', dutyCode: 'SCHEDULING_MANAGEMENT', icon: Calendar, color: '#ffb366' },
  { path: '/doctor-department', label: '科室人员管理', dutyCode: 'DOCTOR_DEPARTMENT_MANAGEMENT', icon: Connection, color: '#4caf50' },
  { path: '/settings', label: '系统设置', dutyCode: 'SYSTEM_SETTINGS', icon: Setting, color: '#909399' }
]

const visibleActions = computed(() => {
  return ACTION_CONFIG.filter(action => authStore.hasDuty(action.dutyCode))
})

const loadStats = async () => {
  const deptCode = authStore.currentDepartmentCode
  const doctorId = authStore.hisStaffId
  if (!deptCode || !doctorId) return
  
  loading.value = true
  try {
    const res = await fetchDashboardStats(deptCode, doctorId)
    if (res.code === 0 && res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})

watch(
  () => authStore.currentDepartmentId,
  (newVal, oldVal) => {
    if (newVal !== oldVal && newVal) {
      loadStats()
    }
  }
)
</script>

<style>
.dashboard-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 32px;
}

.dashboard-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.dept-name {
  font-weight: 500;
  color: var(--color-primary-DEFAULT);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.quick-actions {
  margin-bottom: 32px;
}

.quick-actions h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.action-label {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}
</style>