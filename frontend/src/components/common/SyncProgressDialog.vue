<template>
  <el-dialog
    v-model="visible"
    title=""
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    :destroy-on-close="true"
    class="sync-progress-dialog"
    modal-class="sync-modal"
  >
    <div class="sync-container">
      <!-- 头部 -->
      <div class="sync-header">
        <div class="header-icon">
          <el-icon class="sync-icon" :class="{ 'syncing': isSyncing }">
            <Refresh />
          </el-icon>
        </div>
        <div class="header-text">
          <h2>{{ isSyncing ? '正在同步数据...' : '同步完成' }}</h2>
          <p>{{ syncStatusText }}</p>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="progress-section">
        <el-progress
          :percentage="overallProgress"
          :stroke-width="20"
          :text-inside="true"
          :status="progressStatus"
        />
      </div>

      <!-- 同步项目列表 -->
      <div class="sync-items">
        <div class="sync-item" v-for="(item, index) in syncItems" :key="index">
          <div class="item-icon">
            <el-icon v-if="item.status === 'pending'" class="icon-pending"><Clock /></el-icon>
            <el-icon v-else-if="item.status === 'syncing'" class="icon-syncing Loading"><Loading /></el-icon>
            <el-icon v-else-if="item.status === 'success'" class="icon-success"><SuccessFilled /></el-icon>
            <el-icon v-else-if="item.status === 'failed'" class="icon-failed"><CircleCloseFilled /></el-icon>
          </div>
          <div class="item-content">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-status" :class="item.status">
              {{ getStatusText(item) }}
            </div>
          </div>
          <div class="item-count" v-if="item.count">
            {{ item.count }}条
          </div>
        </div>
      </div>

      <!-- 提示信息 -->
      <div class="sync-tip" v-if="isSyncing">
        <el-icon><InfoFilled /></el-icon>
        <span>同步过程中请勿关闭页面或进行其他操作</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { Refresh, Clock, Loading, SuccessFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'complete', success: boolean): void
}

interface SyncItem {
  name: string
  status: 'pending' | 'syncing' | 'success' | 'failed'
  count?: number
  message?: string
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const isSyncing = ref(true)

const syncItems = ref<SyncItem[]>([
  { name: 'HIS诊断信息同步', status: 'pending' },
  { name: 'HIS医嘱信息同步', status: 'pending' },
  { name: 'HIS科室患者信息总览同步', status: 'pending' }
])

const currentSyncIndex = ref(0)

const cleanupOverlays = () => {
  const overlays = document.querySelectorAll('.el-overlay, .el-overlay-modal, .el-dialog__wrapper')
  overlays.forEach(el => {
    if (el.parentNode) {
      el.parentNode.removeChild(el)
    }
  })
  document.body.classList.remove('el-popup-parent--hidden')
  document.body.style.overflow = ''
}

watch(
  () => props.modelValue,
  (val) => {
    console.log('[SyncProgressDialog] props.modelValue changed:', val)
    visible.value = val
    if (val) {
      console.log('[SyncProgressDialog] calling startSync()')
      startSync()
    }
  },
  { immediate: true }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    nextTick(() => {
      cleanupOverlays()
    })
  }
})

const overallProgress = computed(() => {
  const completed = syncItems.value.filter(item => item.status === 'success' || item.status === 'failed').length
  return Math.round((completed / syncItems.value.length) * 100)
})

const progressStatus = computed(() => {
  if (!isSyncing.value) {
    const hasFailed = syncItems.value.some(item => item.status === 'failed')
    return hasFailed ? 'warning' : 'success'
  }
  return undefined
})

const syncStatusText = computed(() => {
  if (!isSyncing.value) {
    const successCount = syncItems.value.filter(item => item.status === 'success').length
    const failedCount = syncItems.value.filter(item => item.status === 'failed').length
    
    if (failedCount === 0) {
      return `已完成同步，共${syncItems.value.length}项`
    } else {
      return `同步完成，成功${successCount}项，失败${failedCount}项`
    }
  }
  
  const currentItem = syncItems.value[currentSyncIndex.value]
  return currentItem ? `正在同步：${currentItem.name}` : '准备同步...'
})

const getStatusText = (item: SyncItem): string => {
  switch (item.status) {
    case 'pending':
      return '等待同步'
    case 'syncing':
      return '正在同步...'
    case 'success':
      return item.message || `同步成功，新增${item.count || 0}条`
    case 'failed':
      return item.message || '同步失败'
    default:
      return ''
  }
}

const startSync = async () => {
  isSyncing.value = true
  currentSyncIndex.value = 0
  
  // 重置所有状态
  syncItems.value.forEach(item => {
    item.status = 'pending'
    item.count = undefined
    item.message = undefined
  })
  
  // 获取科室编码
  const deptCode = localStorage.getItem('currentDepartmentCode') || ''
  
  // 调用后端批量同步 API，添加超时控制
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), 30000)
  
  // 获取 token 用于认证
  const token = localStorage.getItem('token')

  try {
    const response = await fetch(`/api/sync/execute-batch?deptCode=${encodeURIComponent(deptCode)}&syncType=DEPT_SWITCH`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      signal: controller.signal
    })
    
    clearTimeout(timeoutId)
    
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      const batchResult = result.data
      
      // 更新同步项状态
      if (batchResult.items && batchResult.items.length > 0) {
        syncItems.value = batchResult.items.map((item: any) => ({
          name: item.configName,
          status: item.success ? 'success' : 'failed',
          count: item.totalCount || 0,
          message: item.message
        }))
      }
      
      isSyncing.value = false
      
      setTimeout(() => {
        const success = batchResult.failedCount === 0
        visible.value = false
        emit('update:modelValue', false)
        emit('complete', success)
        nextTick(() => cleanupOverlays())
      }, 800)
    } else {
      // API 调用失败
      syncItems.value.forEach(item => {
        item.status = 'failed'
        item.message = result.message || '同步失败'
      })
      
      isSyncing.value = false
      
      setTimeout(() => {
        visible.value = false
        emit('update:modelValue', false)
        emit('complete', false)
        nextTick(() => cleanupOverlays())
      }, 800)
    }
  } catch (error: any) {
    clearTimeout(timeoutId)
    
    // 异常处理
    const errorMsg = error.name === 'AbortError' ? '同步超时，请稍后重试' : (error.message || '同步失败')
    
    syncItems.value.forEach(item => {
      item.status = 'failed'
      item.message = errorMsg
    })
    
    isSyncing.value = false
    
    setTimeout(() => {
        visible.value = false
        emit('update:modelValue', false)
        emit('complete', false)
        nextTick(() => cleanupOverlays())
      }, 800)
  }
}

const executeSyncApi = async (configId: number, deptCode: string): Promise<{ success: boolean; count: number; message: string }> => {
  const token = localStorage.getItem('token')
  try {
    const response = await fetch(`/api/sync/execute/${configId}?deptCode=${encodeURIComponent(deptCode)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    })
    
    const result = await response.json()
    
    if (result.code === 0 && result.data?.success) {
      return {
        success: true,
        count: result.data.totalCount || 0,
        message: `同步成功，新增${result.data.insertCount || 0}条，更新${result.data.updateCount || 0}条`
      }
    } else {
      return {
        success: false,
        count: 0,
        message: result.message || '同步失败'
      }
    }
  } catch (error: any) {
    return {
      success: false,
      count: 0,
      message: error.message || '同步失败'
    }
  }
}
</script>

<style>
.sync-progress-dialog .el-dialog__header {
  display: none;
}

.sync-progress-dialog .el-dialog__body {
  padding: 0;
}

.sync-modal {
  background-color: rgba(0, 0, 0, 0.5) !important;
}

.sync-container {
  padding: 32px;
}

.sync-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffb366 0%, #ff9443 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sync-icon {
  font-size: 28px;
  color: white;
}

.sync-icon.syncing {
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.header-text h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.header-text p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.progress-section {
  margin-bottom: 24px;
}

.sync-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.sync-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.item-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-pending {
  font-size: 20px;
  color: var(--text-placeholder);
}

.icon-syncing {
  font-size: 20px;
  color: var(--color-primary-DEFAULT);
  animation: rotate 1s linear infinite;
}

.icon-success {
  font-size: 20px;
  color: var(--color-success);
}

.icon-failed {
  font-size: 20px;
  color: var(--color-danger);
}

.item-content {
  flex: 1;
}

.item-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.item-status {
  font-size: 13px;
  margin-top: 2px;
}

.item-status.pending {
  color: var(--text-placeholder);
}

.item-status.syncing {
  color: var(--color-primary-DEFAULT);
}

.item-status.success {
  color: var(--color-success);
}

.item-status.failed {
  color: var(--color-danger);
}

.item-count {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary-DEFAULT);
  padding: 4px 12px;
  background: var(--color-primary-50);
  border-radius: 4px;
}

.sync-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff7ed;
  border-radius: 8px;
  font-size: 13px;
  color: #ea580c;
}

.sync-tip .el-icon {
  font-size: 16px;
}
</style>