<template>
  <div class="doctor-dept-container">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </div>
      <h1 class="page-title">
        <el-icon class="title-icon"><Connection /></el-icon>
        科室人员管理
      </h1>
      <div class="header-right"></div>
    </div>

    <div class="content-wrapper">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input v-model="searchKeyword" placeholder="姓名/工号/科室搜索" clearable style="width: 240px" @input="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增关系
          </el-button>
        </div>
      </div>

      <el-table
        :data="list"
        style="width: 100%"
        class="doctor-dept-table"
        :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
        v-loading="loading"
      >
        <el-table-column prop="doctorUsercode" label="工号" width="120" />
        <el-table-column prop="doctorName" label="医护人员姓名" width="140">
          <template #default="{ row }">
            <span class="name-text">{{ row.doctorName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="departmentCode" label="科室编码" width="120" />
        <el-table-column prop="departmentName" label="科室名称" min-width="140" />
        <el-table-column prop="isPrimary" label="是否主科室" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isPrimary ? 'success' : 'info'" effect="light" size="small">
              {{ row.isPrimary ? '主科室' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="pagination.pageSizes"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑科室人员关系' : '新增科室人员关系'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="医护人员" required>
          <el-select
            v-model="formData.doctorId"
            placeholder="请选择医护人员"
            style="width: 100%"
            filterable
            :disabled="!!editingItem"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.username} (${user.usercode || '-'})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科室" required>
          <el-select
            v-model="formData.departmentId"
            placeholder="请选择科室"
            style="width: 100%"
            filterable
            :disabled="!!editingItem"
          >
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="`${dept.name} (${dept.code})`"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否主科室">
          <el-switch v-model="formData.isPrimary" />
          <span class="switch-hint">同一医护人员只能有一个主科室</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Connection, Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import {
  fetchDoctorDepartmentList,
  createDoctorDepartment,
  updateDoctorDepartment,
  deleteDoctorDepartment,
  type DoctorDepartmentManagementDto
} from '@/api/doctorDepartmentManagement'
import { fetchUserList, type User } from '@/api/user'
import { fetchDepartmentList, type DepartmentDto } from '@/api/department'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingItem = ref<DoctorDepartmentManagementDto | null>(null)
const searchKeyword = ref('')
const list = ref<DoctorDepartmentManagementDto[]>([])
const userList = ref<User[]>([])
const departmentList = ref<DepartmentDto[]>([])

const formData = reactive({
  doctorId: null as number | null,
  departmentId: null as number | null,
  isPrimary: false
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  pageSizes: [10, 20, 50, 100]
})

const formatTime = (time: string): string => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  loadList()
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await fetchDoctorDepartmentList(
      pagination.page,
      pagination.pageSize,
      searchKeyword.value
    )
    if (res.code === 0) {
      list.value = res.data.list
      pagination.total = res.data.total
    } else {
      ElMessage.error(res.message || '加载列表失败')
    }
  } catch {
    ElMessage.error('加载列表失败')
  } finally {
    loading.value = false
  }
}

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

const loadDepartmentList = async () => {
  try {
    const res = await fetchDepartmentList()
    if (res.code === 0) {
      departmentList.value = res.data
    }
  } catch {
    ElMessage.error('加载科室列表失败')
  }
}

const handleAdd = () => {
  editingItem.value = null
  formData.doctorId = null
  formData.departmentId = null
  formData.isPrimary = false
  dialogVisible.value = true
}

const handleEdit = (item: DoctorDepartmentManagementDto) => {
  editingItem.value = item
  formData.doctorId = item.doctorId
  formData.departmentId = item.departmentId
  formData.isPrimary = item.isPrimary
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.doctorId || !formData.departmentId) {
    ElMessage.warning('请选择医护人员和科室')
    return
  }

  saving.value = true
  try {
    if (editingItem.value) {
      const res = await updateDoctorDepartment(editingItem.value.id, formData.isPrimary)
      if (res.code === 0) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadList()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await createDoctorDepartment(
        formData.doctorId,
        formData.departmentId,
        formData.isPrimary
      )
      if (res.code === 0) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadList()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (item: DoctorDepartmentManagementDto) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${item.doctorName}"在"${item.departmentName}"的关系吗？`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await deleteDoctorDepartment(item.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadList()
  loadUserList()
  loadDepartmentList()
})
</script>

<style>
.doctor-dept-container {
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

.doctor-dept-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.name-text {
  font-weight: 500;
  color: var(--text-primary);
}

.time-text {
  color: var(--text-secondary);
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.switch-hint {
  margin-left: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>