<template>
  <div class="statistics">
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>统计分析 <span class="date-badge">{{ today }}</span></h1>
      </div>
    </div>

    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon" style="background: linear-gradient(135deg, #ffb366, #ff9443)">
          <el-icon :size="32"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalPatients }}</div>
          <div class="stat-label">在院患者</div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #529b2e)">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayHandovers }}</div>
          <div class="stat-label">今日交班</div>
          <div class="stat-sub">已完成 {{ stats.completedHandovers }}</div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c, #c93b3b)">
          <el-icon :size="32"><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.criticalPatients }}</div>
          <div class="stat-label">高风险患者</div>
          <div class="stat-sub">MEWS ≥ 3</div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #d4880b)">
          <el-icon :size="32"><List /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingTodos }}</div>
          <div class="stat-label">待办事项</div>
          <div class="stat-sub">未完成</div>
        </div>
      </el-card>
    </div>

    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">患者风险分布（MEWS评分）</span>
        </div>
      </template>
      <div class="risk-distribution">
        <div class="risk-item">
          <div class="risk-label">
            <span class="risk-dot low"></span>
            低风险 (MEWS 0)
          </div>
          <div class="risk-bar-wrapper">
            <div class="risk-bar low" :style="{ width: getRiskPercent('low') + '%' }"></div>
          </div>
          <div class="risk-count">{{ riskDistribution.low }}人</div>
        </div>
        <div class="risk-item">
          <div class="risk-label">
            <span class="risk-dot medium"></span>
            中风险 (MEWS 1-2)
          </div>
          <div class="risk-bar-wrapper">
            <div class="risk-bar medium" :style="{ width: getRiskPercent('medium') + '%' }"></div>
          </div>
          <div class="risk-count">{{ riskDistribution.medium }}人</div>
        </div>
        <div class="risk-item">
          <div class="risk-label">
            <span class="risk-dot high"></span>
            高风险 (MEWS ≥ 3)
          </div>
          <div class="risk-bar-wrapper">
            <div class="risk-bar high" :style="{ width: getRiskPercent('high') + '%' }"></div>
          </div>
          <div class="risk-count">{{ riskDistribution.high }}人</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, User, Document, Warning, List } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getPatientList } from '@/mock/patient'

const router = useRouter()
const authStore = useAuthStore()

const today = new Date().toISOString().slice(0, 10)

const patientList = computed(() => {
  const deptId = authStore.currentDepartmentId
  return getPatientList(deptId || 1)
})

const riskDistribution = computed(() => {
  const patients = patientList.value
  return {
    low: patients.filter(p => p.mewsScore === 0).length,
    medium: patients.filter(p => p.mewsScore >= 1 && p.mewsScore <= 2).length,
    high: patients.filter(p => p.mewsScore >= 3).length
  }
})

const stats = computed(() => {
  const patients = patientList.value
  return {
    totalPatients: patients.length,
    todayHandovers: 3,
    completedHandovers: 1,
    criticalPatients: riskDistribution.value.high,
    pendingTodos: 5
  }
})

const getRiskPercent = (level: 'low' | 'medium' | 'high') => {
  const total = patientList.value.length
  if (total === 0) return 0
  return Math.round((riskDistribution.value[level] / total) * 100)
}
</script>

<style>
.statistics {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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

.date-badge {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  background: rgba(255, 179, 102, 0.15);
  color: var(--color-primary-DEFAULT);
  border-radius: 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1000px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 500px) {
  .stats-grid { grid-template-columns: 1fr; }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: var(--radius-lg);
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

.stat-sub {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 2px;
}

.chart-card {
  border-radius: var(--radius-lg);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.risk-distribution {
  padding: 8px 0;
}

.risk-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.risk-item:last-child {
  margin-bottom: 0;
}

.risk-label {
  width: 140px;
  font-size: 14px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.risk-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.risk-dot.low {
  background: #67c23a;
}

.risk-dot.medium {
  background: #e6a23c;
}

.risk-dot.high {
  background: #f56c6c;
}

.risk-bar-wrapper {
  flex: 1;
  height: 24px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  overflow: hidden;
}

.risk-bar {
  height: 100%;
  border-radius: 12px;
  transition: width 0.3s ease;
}

.risk-bar.low {
  background: linear-gradient(90deg, #67c23a, #95d475);
}

.risk-bar.medium {
  background: linear-gradient(90deg, #e6a23c, #f3d19e);
}

.risk-bar.high {
  background: linear-gradient(90deg, #f56c6c, #fab6b6);
}

.risk-count {
  width: 50px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: right;
}
</style>