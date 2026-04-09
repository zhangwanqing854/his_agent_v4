<template>
  <div class="user-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </div>
      <h1 class="page-title">
        <el-icon class="title-icon"><UserFilled /></el-icon>
        用户管理
      </h1>
      <div class="header-right"></div>
    </div>

    <!-- 主内容区 -->
    <div class="content-wrapper">
      <!-- 筛选和操作栏 -->
      <div class="filter-bar">
        <div class="filter-left">
          <el-input v-model="userFilter.username" placeholder="用户名搜索" clearable style="width: 180px">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="userFilter.roleId" placeholder="角色" clearable style="width: 140px">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
          <el-select v-model="userFilter.enabled" placeholder="状态" clearable style="width: 120px">
            <el-option label="已启用" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button type="primary" @click="handleAddUser">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </div>

      <!-- 用户列表 -->
      <el-table
        :data="filteredUserList"
        style="width: 100%"
        class="user-table"
        :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
      >
        <el-table-column prop="usercode" label="用户编码" width="120" />
        <el-table-column prop="username" label="用户名" width="140">
          <template #default="{ row }">
            <div class="user-name">
              <span class="name-text">{{ row.username }}</span>
              <el-tag v-if="row.isSuperAdmin" type="warning" size="small">超管</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="关联HIS人员" min-width="180">
          <template #default="{ row }">
            <div v-if="row.hisStaff" class="staff-info">
              <span class="staff-name">{{ row.hisStaff.name }}</span>
              <span class="staff-dept">{{ row.hisStaff.departmentName }} - {{ row.hisStaff.title }}</span>
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role?.code === 'SUPER_ADMIN' ? 'warning' : 'success'" effect="light" size="small">
              {{ row.role?.name || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" effect="light" size="small">
              {{ row.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.createdAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditUser(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="!row.isSuperAdmin"
              :type="row.enabled ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleUser(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="!row.isSuperAdmin"
              type="info"
              link
              size="small"
              @click="handleResetPassword(row)"
            >
              <el-icon><Key /></el-icon>
              重置密码
            </el-button>
            <el-button
              v-if="!row.isSuperAdmin"
              type="danger"
              link
              size="small"
              @click="handleDeleteUser(row)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 用户编辑弹窗 -->
    <UserEditDialog
      v-model="userDialogVisible"
      :user="currentUser"
      @success="handleUserSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, UserFilled, Plus, Search, Edit, Delete, Key } from '@element-plus/icons-vue'
import UserEditDialog from '@/components/settings/UserEditDialog.vue'
import {
  fetchUserList,
  fetchRoleList,
  deleteUserApi,
  enableUserApi,
  disableUserApi,
  resetPasswordApi
} from '@/api/user'
import type { User, Role } from '@/types/user'

const router = useRouter()

// 用户筛选
const userFilter = reactive({
  username: '',
  roleId: null as number | null,
  enabled: undefined as boolean | undefined
})

// 用户数据
const userList = ref<User[]>([])
const roleList = ref<Role[]>([])
const userDialogVisible = ref(false)
const currentUser = ref<User | null>(null)

// 筛选后的用户列表
const filteredUserList = computed(() => {
  return userList.value.filter(user => {
    if (userFilter.username && !user.username.includes(userFilter.username)) return false
    if (userFilter.roleId !== null && userFilter.roleId !== undefined && user.roleId !== userFilter.roleId) return false
    if (userFilter.enabled !== undefined && user.enabled !== userFilter.enabled) return false
    return true
  })
})

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await fetchUserList()
    if (res.code === 0) {
      userList.value = res.data
    }
  } catch {
    ElMessage.error('加载用户列表失败')
  }
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const res = await fetchRoleList()
    if (res.code === 0) {
      roleList.value = res.data
    }
  } catch {
    ElMessage.error('加载角色列表失败')
  }
}

// 新增用户
const handleAddUser = () => {
  currentUser.value = null
  userDialogVisible.value = true
}

// 编辑用户
const handleEditUser = (user: User) => {
  currentUser.value = { ...user }
  userDialogVisible.value = true
}

// 启用/禁用用户
const handleToggleUser = async (user: User) => {
  const action = user.enabled ? '禁用' : '启用'
  
  try {
    let result
    if (user.enabled) {
      result = await disableUserApi(user.id)
    } else {
      result = await enableUserApi(user.id)
    }
    
    if (result.code === 0) {
      ElMessage.success(`已${action}`)
      loadUserList()
    } else {
      ElMessage.error(result.message)
    }
  } catch {
    ElMessage.error(`${action}失败`)
  }
}

// 重置密码
const handleResetPassword = async (user: User) => {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.{6,32}$/,
    inputErrorMessage: '密码长度为6-32个字符'
  }).then(async ({ value }) => {
    try {
      const result = await resetPasswordApi(user.id, value)
      if (result.code === 0) {
        ElMessage.success('密码重置成功')
      } else {
        ElMessage.error(result.message)
      }
    } catch {
      ElMessage.error('密码重置失败')
    }
  }).catch(() => {})
}

// 删除用户
const handleDeleteUser = async (user: User) => {
  ElMessageBox.confirm(`确定要删除用户"${user.username}"吗？此操作不可恢复。`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const result = await deleteUserApi(user.id)
      if (result.code === 0) {
        ElMessage.success('删除成功')
        loadUserList()
      } else {
        ElMessage.error(result.message)
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 用户操作成功回调
const handleUserSuccess = () => {
  loadUserList()
}

// 初始化
onMounted(() => {
  loadUserList()
  loadRoleList()
})
</script>

<style>
.user-management-container {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.page-header {
  height: 64px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left,
.header-right {
  width: 100px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.title-icon {
  color: var(--color-primary-DEFAULT);
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  margin-top: 24px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-left {
  display: flex;
  gap: 12px;
}

.filter-right {
  display: flex;
  gap: 12px;
}

.user-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
  color: var(--text-primary);
}

.staff-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.staff-name {
  font-weight: 500;
  color: var(--text-primary);
}

.staff-dept {
  font-size: 12px;
  color: var(--text-secondary);
}

.no-data {
  color: var(--text-placeholder);
}

.time-text {
  color: var(--text-secondary);
  font-size: 13px;
}
</style>