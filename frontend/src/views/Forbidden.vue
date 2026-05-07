<template>
  <div class="forbidden-container">
    <div class="forbidden-content">
      <el-icon :size="80" color="#e6a23c"><WarningFilled /></el-icon>
      <h1>403</h1>
      <h2>无权限访问</h2>
      <p class="description">您没有访问此页面的权限，请联系管理员分配相应职责。</p>
      <el-button type="primary" @click="router.push('/')">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { WarningFilled, HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.forbidden-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.forbidden-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-width: 400px;
}

.forbidden-content h1 {
  font-size: 72px;
  color: #e6a23c;
  margin: 16px 0 8px;
  font-weight: 700;
}

.forbidden-content h2 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 16px;
}

.description {
  color: #909399;
  margin-bottom: 24px;
  font-size: 14px;
}
</style>