<template>
  <div class="todo-list">
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1>待办事项</h1>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="todo-tabs">
      <el-tab-pane label="待处理交班" name="pending">
        <div class="todo-section">
          <div v-if="pendingHandovers.length === 0" class="empty-state">
            <el-empty description="暂无待处理交班" />
          </div>
          <div v-else class="todo-items">
            <div
              v-for="item in pendingHandovers"
              :key="item.id"
              class="todo-item"
              @click="handleHandover(item)"
            >
              <div class="todo-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-meta">
                  <span>{{ item.department }}</span>
                  <span>{{ item.time }}</span>
                </div>
              </div>
              <div class="todo-action">
                <el-tag type="warning" size="small">待处理</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="重点关注患者" name="critical">
        <div class="todo-section">
          <div v-if="criticalPatients.length === 0" class="empty-state">
            <el-empty description="暂无重点关注患者" />
          </div>
          <div v-else class="todo-items">
            <div
              v-for="item in criticalPatients"
              :key="item.id"
              class="todo-item"
              @click="handlePatient(item)"
            >
              <div class="todo-icon critical">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ item.name }} - {{ item.bedNumber }}</div>
                <div class="todo-meta">
                  <span>{{ item.diagnosis }}</span>
                  <span>MEWS: {{ item.mewsScore }}</span>
                </div>
              </div>
              <div class="todo-action">
                <el-tag type="danger" size="small">高风险</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="待审核记录" name="review">
        <div class="todo-section">
          <div v-if="reviewRecords.length === 0" class="empty-state">
            <el-empty description="暂无待审核记录" />
          </div>
          <div v-else class="todo-items">
            <div
              v-for="item in reviewRecords"
              :key="item.id"
              class="todo-item"
              @click="handleReview(item)"
            >
              <div class="todo-icon review">
                <el-icon><Document /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-meta">
                  <span>{{ item.doctor }}</span>
                  <span>{{ item.time }}</span>
                </div>
              </div>
              <div class="todo-action">
                <el-tag type="info" size="small">待审核</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Clock, Warning, Document } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('pending')

const pendingHandovers = ref([
  { id: 1, title: '心内科夜班交班', department: '心内科', time: '今日 20:00' },
  { id: 2, title: '神经内科白班交班', department: '神经内科', time: '明日 08:00' }
])

const criticalPatients = ref([
  { id: 3, name: '王五', bedNumber: '3床', diagnosis: '急性心肌梗死', mewsScore: 3 },
  { id: 12, name: '王十二', bedNumber: '2床', diagnosis: '脑出血', mewsScore: 3 },
  { id: 13, name: '陈十三', bedNumber: '3床', diagnosis: '癫痫持续状态', mewsScore: 4 }
])

const reviewRecords = ref([
  { id: 1, title: '交班记录审核', doctor: '张医生', time: '2026-03-29 16:00' }
])

const handleHandover = (item: { id: number }) => {
  router.push(`/handovers?id=${item.id}`)
}

const handlePatient = (item: { id: number }) => {
  router.push(`/patients?highlight=${item.id}`)
}

const handleReview = (item: { id: number }) => {
  router.push(`/handovers?id=${item.id}`)
}
</script>

<style>
.todo-list {
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
  margin: 0;
}

.todo-tabs {
  margin-top: 16px;
}

.todo-section {
  min-height: 300px;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.todo-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
}

.todo-item:hover {
  border-color: var(--color-primary-DEFAULT);
  box-shadow: 0 2px 8px rgba(255, 179, 102, 0.2);
}

.todo-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.todo-icon.pending {
  background: linear-gradient(135deg, #e6a23c, #d4880b);
}

.todo-icon.critical {
  background: linear-gradient(135deg, #f56c6c, #c93b3b);
}

.todo-icon.review {
  background: linear-gradient(135deg, #409eff, #337ecc);
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.todo-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
}

.todo-action {
  flex-shrink: 0;
}
</style>