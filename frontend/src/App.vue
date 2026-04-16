<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { RouterView, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import TopNav from './components/layout/TopNav.vue'
import SyncProgressDialog from './components/common/SyncProgressDialog.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showSyncDialog = ref(false)
const isInitialLogin = ref(false)
const syncTimeoutId = ref<number | null>(null)

const cleanupOverlays = () => {
  const overlays = document.querySelectorAll('.el-overlay, .el-overlay-modal, .el-dialog__wrapper')
  overlays.forEach(el => {
    if (el.parentNode) {
      el.parentNode.removeChild(el)
    }
  })
  document.body.classList.remove('el-popup-parent--hidden')
  document.body.style.overflow = ''
  console.log('[App] cleaned up overlays:', overlays.length)
}

const startSyncWithTimeout = () => {
  showSyncDialog.value = true
  syncTimeoutId.value = window.setTimeout(() => {
    if (showSyncDialog.value) {
      console.log('[App] sync timeout, forcing close')
      showSyncDialog.value = false
      cleanupOverlays()
      if (isInitialLogin.value) {
        isInitialLogin.value = false
        ElMessage.warning('数据同步超时，请稍后在系统设置中手动同步')
        router.push('/handovers/create')
      }
    }
  }, 35000)
}

watch(
  () => authStore.isLoggedIn,
  (newValue, oldValue) => {
    if (newValue && !oldValue) {
      isInitialLogin.value = true
      setTimeout(() => {
        startSyncWithTimeout()
      }, 100)
    }
  }
)

watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/handovers') {
      nextTick(() => {
        cleanupOverlays()
      })
    }
  }
)

onMounted(async () => {
  if (authStore.isLoggedIn && !authStore.userInfo) {
    try {
      await authStore.fetchUserInfo()
    } catch {
      authStore.logout()
      router.push('/login')
    }
  }
  if (authStore.isLoggedIn) {
    isInitialLogin.value = false
  }
})

onUnmounted(() => {
  if (syncTimeoutId.value) {
    clearTimeout(syncTimeoutId.value)
  }
})

const handleSyncComplete = (success: boolean) => {
  console.log('[App] sync complete, success:', success, 'isInitialLogin:', isInitialLogin.value)
  if (syncTimeoutId.value) {
    clearTimeout(syncTimeoutId.value)
    syncTimeoutId.value = null
  }
  
  showSyncDialog.value = false
  
  nextTick(() => {
    cleanupOverlays()
  })
  
  if (isInitialLogin.value) {
    isInitialLogin.value = false
    if (!success) {
      ElMessage.warning('部分数据同步失败，请稍后在系统设置中手动同步')
    }
    router.push('/handovers/create')
  }
}
</script>

<template>
  <div v-if="authStore.isLoggedIn" class="app-container">
    <TopNav />
    <div class="main-content">
      <RouterView />
    </div>
    <!-- 登录后的同步进度对话框 -->
    <SyncProgressDialog
      v-model="showSyncDialog"
      @complete="handleSyncComplete"
    />
  </div>
  <RouterView v-else />
</template>

<style>
.app-container {
  min-height: 100vh;
}

.main-content {
  margin-top: 64px;
}
</style>