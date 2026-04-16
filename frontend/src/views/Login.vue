<template>
  <div class="login-container">
    <div class="decoration decoration-1">🏥</div>
    <div class="decoration decoration-2">💊</div>
    <div class="decoration decoration-3">❤️</div>
    
    <div class="login-card glass">
      <div class="login-header">
        <h1>医护智能交接班系统</h1>
        <p class="subtitle">北京大学国际医院</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="usercode">
          <el-input
            v-model="loginForm.usercode"
            placeholder="请输入用户编码"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button btn-primary-gradient"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p>默认账号：<strong>admin</strong> / <strong>admin</strong></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  usercode: 'admin',
  password: 'admin'
})

const rules: FormRules = {
  usercode: [{ required: true, message: '请输入用户编码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  console.log('[Login] form values:', loginForm)
  
  try {
    await formRef.value.validate()
  } catch {
    console.log('[Login] form validation failed')
    return
  }
  
  loading.value = true
  console.log('[Login] calling authStore.login...')
  
  try {
    const success = await authStore.login(
      loginForm.usercode,
      loginForm.password
    )
    
    if (!success) {
      // 登录失败，authStore.login 已经显示了错误消息
      loading.value = false
      return
    }
    
    console.log('[Login] login success')
    ElMessage.success('登录成功')
    
    // 登录成功后，App.vue 会检测到 isLoggedIn 变化并触发同步
    // 这里只需要结束 loading 状态
    loading.value = false
  } catch (error: any) {
    console.error('[Login] login error:', error)
    ElMessage.error(error.message || '登录失败')
    loading.value = false
  }
}
</script>

<style>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 50%, #fed7aa 100%);
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 179, 102, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 148, 67, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(255, 237, 213, 0.5) 0%, transparent 40%);
  animation: float 20s ease-in-out infinite;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(to top, rgba(255, 179, 102, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(5deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(-5deg);
  }
}

.login-card {
  width: 420px;
  padding: 48px;
  border-radius: var(--radius-xl);
  position: relative;
  z-index: 1;
  box-shadow: 
    0 20px 60px rgba(255, 179, 102, 0.15),
    0 8px 25px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.login-form {
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.login-tips {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}

.login-tips p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.login-tips strong {
  color: var(--color-primary-light);
}

/* 装饰性医疗图标 */
.login-container .decoration {
  position: absolute;
  font-size: 80px;
  opacity: 0.1;
  color: var(--color-primary-DEFAULT);
  z-index: 0;
}

.login-container .decoration-1 {
  top: 10%;
  left: 10%;
  animation: pulse 4s ease-in-out infinite;
}

.login-container .decoration-2 {
  bottom: 15%;
  right: 15%;
  animation: pulse 4s ease-in-out infinite 2s;
}

.login-container .decoration-3 {
  top: 60%;
  left: 5%;
  font-size: 60px;
  animation: pulse 4s ease-in-out infinite 1s;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.1;
    transform: scale(1);
  }
  50% {
    opacity: 0.15;
    transform: scale(1.05);
  }
}
</style>