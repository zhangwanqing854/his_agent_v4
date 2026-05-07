<template>
  <div class="top-nav">
    <div class="nav-left">
      <div class="logo">🏥</div>
      <span class="system-name">医护智能交接班系统</span>
    </div>

    <div class="nav-right">
      <el-select
        v-if="authStore.canSwitchDepartment"
        v-model="currentDeptId"
        class="dept-selector"
        size="small"
        filterable
        @change="handleDepartmentChange"
      >
        <el-option
          v-for="dept in authStore.userDepartments"
          :key="dept.id"
          :label="dept.name"
          :value="dept.id"
        >
          <span>{{ dept.name }}</span>
          <span v-if="dept.isPrimary" class="primary-badge">主</span>
        </el-option>
      </el-select>
      <div v-else class="current-dept">
        {{ authStore.currentDepartmentName }}
      </div>
      
      <el-button
        v-if="!isCurrentPrimary"
        type="primary"
        size="small"
        plain
        @click="handleSetPrimary"
        :loading="settingPrimary"
      >
        设为主科室
      </el-button>
      
      <div class="user-info">
        <el-icon :size="20"><User /></el-icon>
        <span>{{ authStore.userName }}</span>
      </div>
      <el-tooltip content="系统设置" placement="bottom">
        <el-button class="settings-btn" @click="router.push('/settings')">
          <el-icon :size="18"><Setting /></el-icon>
        </el-button>
      </el-tooltip>
      <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
    </div>

    <!-- 同步进度弹窗 -->
    <SyncProgressDialog
      v-model="showSyncDialog"
      @complete="handleSyncComplete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SyncProgressDialog from '@/components/common/SyncProgressDialog.vue'
import { setPrimaryDepartment } from '@/api/doctorDepartmentManagement'

const router = useRouter()
const authStore = useAuthStore()
const showSyncDialog = ref(false)
const syncTimeoutId = ref<number | null>(null)
const settingPrimary = ref(false)

const currentDeptId = computed({
  get: () => authStore.currentDepartmentId,
  set: (val) => authStore.switchDepartment(val as number)
})

const isCurrentPrimary = computed(() => {
  const currentDept = authStore.userDepartments.find(d => d.id === authStore.currentDepartmentId)
  return currentDept?.isPrimary || false
})

const handleDepartmentChange = (deptId: number) => {
  console.log('[TopNav] handleDepartmentChange called, deptId:', deptId)
  console.log('[TopNav] before switchDepartment, currentDeptId:', authStore.currentDepartmentId)
  
  authStore.switchDepartment(deptId)
  
  console.log('[TopNav] after switchDepartment, currentDeptId:', authStore.currentDepartmentId)
  console.log('[TopNav] setting showSyncDialog = true')
  
  showSyncDialog.value = true
  
  console.log('[TopNav] showSyncDialog.value:', showSyncDialog.value)
  
  syncTimeoutId.value = window.setTimeout(() => {
    if (showSyncDialog.value) {
      console.log('[TopNav] sync timeout, forcing close')
      showSyncDialog.value = false
      ElMessage.warning('数据同步超时，请稍后在系统设置中手动同步')
    }
  }, 35000)
}

onUnmounted(() => {
  if (syncTimeoutId.value) {
    clearTimeout(syncTimeoutId.value)
  }
})

const handleSyncComplete = (success: boolean) => {
  console.log('[TopNav] sync complete, success:', success)
  if (syncTimeoutId.value) {
    clearTimeout(syncTimeoutId.value)
    syncTimeoutId.value = null
  }
  
  if (!success) {
    ElMessage.warning('部分数据同步失败，请稍后在系统设置中手动同步')
  }
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}

const handleSetPrimary = async () => {
  const deptName = authStore.currentDepartmentName
  const doctorId = authStore.userInfo?.id
  const departmentId = authStore.currentDepartmentId
  
  if (!doctorId || !departmentId) {
    ElMessage.error('无法获取用户或科室信息')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定将"${deptName}"设为您的主科室吗？`,
      '设置主科室',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    settingPrimary.value = true
    const res = await setPrimaryDepartment(doctorId, departmentId)
    
    if (res.code === 0) {
      ElMessage.success('已设置为主科室')
      await authStore.fetchUserInfo()
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch {
    // 用户取消
  } finally {
    settingPrimary.value = false
  }
}
</script>

<style>
.top-nav {
  height: 64px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  font-size: 28px;
}

.system-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dept-selector {
  width: 120px;
}

.current-dept {
  padding: 0 12px;
  height: 32px;
  line-height: 32px;
  background: rgba(255, 179, 102, 0.1);
  border-radius: 4px;
  color: var(--color-primary-DEFAULT);
  font-size: 14px;
}

.primary-badge {
  margin-left: 6px;
  padding: 0 4px;
  background: var(--color-primary-DEFAULT);
  color: white;
  font-size: 10px;
  border-radius: 2px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-primary);
}

.settings-btn {
  padding: 8px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.settings-btn:hover {
  background: var(--color-primary-50);
  border-color: var(--color-primary-DEFAULT);
  color: var(--color-primary-DEFAULT);
}
</style>
