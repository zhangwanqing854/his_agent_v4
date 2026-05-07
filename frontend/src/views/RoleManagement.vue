<template>
  <div class="role-management-container">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </div>
      <h1 class="page-title">
        <el-icon class="title-icon"><Key /></el-icon>
        角色权限管理
      </h1>
      <div class="header-right"></div>
    </div>

    <div class="content-wrapper">
      <el-tabs v-model="activeTab" class="role-tabs">
        <el-tab-pane label="角色管理" name="roles">
          <div class="filter-bar">
            <div class="filter-left">
              <el-input v-model="roleFilter.name" placeholder="角色名称搜索" clearable style="width: 180px">
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="filter-right">
              <el-button type="primary" @click="handleAddRole">
                <el-icon><Plus /></el-icon>
                新增角色
              </el-button>
            </div>
          </div>

          <el-table
            :data="filteredRoleList"
            style="width: 100%"
            class="role-table"
            :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
          >
            <el-table-column prop="name" label="角色名称" width="150">
              <template #default="{ row }">
                <div class="role-name">
                  <span class="name-text">{{ row.name }}</span>
                  <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="code" label="角色编码" width="140" align="center">
              <template #default="{ row }">
                <el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" />
            <el-table-column label="职责数量" width="100" align="center">
              <template #default="{ row }">
                <el-tag effect="light" size="small">{{ row.duties?.length || 0 }} 个</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditRole(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="info" link size="small" @click="handleViewDuties(row)">
                  <el-icon><View /></el-icon>
                  查看职责
                </el-button>
                <el-button
                  v-if="!['SUPER_ADMIN', 'DOCTOR'].includes(row.code)"
                  type="danger"
                  link
                  size="small"
                  @click="handleDeleteRole(row)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="职责管理" name="duties">
          <el-table
            :data="dutyList"
            style="width: 100%"
            class="duty-table"
            :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
          >
            <el-table-column prop="name" label="职责名称" width="150" />
            <el-table-column prop="code" label="职责编码" width="160" align="center">
              <template #default="{ row }">
                <el-tag type="info" effect="light" size="small">{{ row.code }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <RoleEditDialog
      v-model="roleDialogVisible"
      :role="currentRole"
      :duties="dutyList"
      @success="handleRoleSuccess"
    />

    <RoleDutiesDialog
      v-model="dutiesDialogVisible"
      :role="currentRole"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Key, Plus, Search, Edit, Delete, View } from '@element-plus/icons-vue'
import {
  fetchRoleList,
  fetchDutyList,
  deleteRoleApi
} from '@/api/user'
import type { Role, Duty } from '@/types/user'
import RoleEditDialog from '@/components/settings/RoleEditDialog.vue'
import RoleDutiesDialog from '@/components/settings/RoleDutiesDialog.vue'

const router = useRouter()

const activeTab = ref('roles')
const roleList = ref<Role[]>([])
const dutyList = ref<Duty[]>([])

const roleFilter = reactive({
  name: ''
})

const roleDialogVisible = ref(false)
const dutiesDialogVisible = ref(false)
const currentRole = ref<Role | null>(null)

const filteredRoleList = computed(() => {
  if (!roleFilter.name) return roleList.value
  return roleList.value.filter(r => 
    r.name.includes(roleFilter.name) || r.code.includes(roleFilter.name)
  )
})

const loadData = async () => {
  try {
    const [roleRes, dutyRes] = await Promise.all([
      fetchRoleList(),
      fetchDutyList()
    ])
    
    if (roleRes.code === 0) roleList.value = roleRes.data
    if (dutyRes.code === 0) dutyList.value = dutyRes.data
  } catch {
    ElMessage.error('加载数据失败')
  }
}

const handleAddRole = () => {
  currentRole.value = null
  roleDialogVisible.value = true
}

const handleEditRole = (role: Role) => {
  currentRole.value = { ...role, duties: role.duties ? [...role.duties] : [] }
  roleDialogVisible.value = true
}

const handleViewDuties = (role: Role) => {
  currentRole.value = role
  dutiesDialogVisible.value = true
}

const handleDeleteRole = async (role: Role) => {
  ElMessageBox.confirm(`确定要删除角色"${role.name}"吗？此操作不可恢复。`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const result = await deleteRoleApi(role.id)
      if (result.code === 0) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(result.message)
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleRoleSuccess = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style>
.role-management-container {
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

.role-tabs {
  background: transparent;
}

.role-tabs .el-tabs__header {
  margin-bottom: 20px;
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

.role-table,
.duty-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.role-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
  color: var(--text-primary);
}
</style>