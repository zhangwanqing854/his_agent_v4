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

    <div class="stats-grid">
      <el-card class="stat-card clickable" shadow="hover" @click="router.push('/patients')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #ffb366, #ff9443)">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalPatients }}</div>
          <div class="stat-label">在院患者</div>
        </div>
      </el-card>

      <el-card class="stat-card clickable" shadow="hover" @click="router.push('/handovers?tab=completed')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #529b2e)">
          <el-icon :size="32"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.completedHandovers }}</div>
          <div class="stat-label">已完成交班</div>
        </div>
      </el-card>

      <el-card class="stat-card clickable" shadow="hover" @click="router.push('/handovers?tab=pending')">
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
        <el-card class="action-card" shadow="hover" @click="router.push('/handovers/create')">
          <el-icon :size="40" color="#ffb366"><Document /></el-icon>
          <div class="action-label">发起交班</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/patients')">
          <el-icon :size="40" color="#67c23a"><User /></el-icon>
          <div class="action-label">科室患者</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/statistics')">
          <el-icon :size="40" color="#e6a23c"><DataAnalysis /></el-icon>
          <div class="action-label">统计分析</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/users')">
          <el-icon :size="40" color="#9c27b0"><UserFilled /></el-icon>
          <div class="action-label">用户管理</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/roles')">
          <el-icon :size="40" color="#ff5722"><Key /></el-icon>
          <div class="action-label">角色权限</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/his-staff')">
          <el-icon :size="40" color="#00bcd4"><Avatar /></el-icon>
          <div class="action-label">人员管理</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/departments')">
          <el-icon :size="40" color="#9c27b0"><OfficeBuilding /></el-icon>
          <div class="action-label">科室管理</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/scheduling')">
          <el-icon :size="40" color="#ffb366"><Calendar /></el-icon>
          <div class="action-label">科室排班</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="router.push('/settings')">
          <el-icon :size="40" color="#909399"><Setting /></el-icon>
          <div class="action-label">系统设置</div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchDashboardStats, type DashboardStatsDto } from '@/api/dashboardStats'
import { User, Document, CircleCheck, Bell, DataAnalysis, UserFilled, Key, Setting, Avatar, OfficeBuilding, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const stats = ref<DashboardStatsDto>({
  totalPatients: 0,
  completedHandovers: 0,
  pendingHandovers: 0
})

const loading = ref(false)

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
