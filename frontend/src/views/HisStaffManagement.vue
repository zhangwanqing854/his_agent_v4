<template>
  <div class="his-staff-container">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </div>
      <h1 class="page-title">
        <el-icon class="title-icon"><Avatar /></el-icon>
        人员管理
      </h1>
      <div class="header-right"></div>
    </div>

    <div class="content-wrapper">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input v-model="filter.name" placeholder="姓名搜索" clearable style="width: 180px">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filter.departmentId" placeholder="所属科室" clearable style="width: 160px" filterable>
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增人员
          </el-button>
        </div>
      </div>

      <el-table
        :data="paginatedStaff"
        style="width: 100%"
        class="staff-table"
        :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
      >
        <el-table-column prop="staffCode" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="120">
          <template #default="{ row }">
            <span class="name-text">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属科室" min-width="140">
          <template #default="{ row }">
            <span v-if="row.departmentName">{{ row.departmentName }}</span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职称" width="140">
          <template #default="{ row }">
            <span v-if="row.title">{{ row.title }}</span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="140">
          <template #default="{ row }">
            <span v-if="row.phone">{{ row.phone }}</span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="syncTime" label="同步时间" width="170" align="center">
          <template #default="{ row }">
            <span v-if="row.syncTime" class="time-text">{{ row.syncTime }}</span>
            <span v-else class="no-data">-</span>
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
      :title="editingStaff ? '编辑人员' : '新增人员'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="80px">
        <el-form-item label="工号" required>
          <el-input v-model="formData.staffCode" :disabled="!!editingStaff" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="所属科室">
          <el-select v-model="formData.departmentId" placeholder="请选择科室" style="width: 100%" filterable>
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="formData.title" placeholder="请输入职称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Avatar, Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import {
  fetchHisStaffList,
  createHisStaff,
  updateHisStaff,
  deleteHisStaff,
  type HisStaffDto,
  type HisStaffCreateRequest,
  type HisStaffUpdateRequest
} from '@/api/hisStaff'
import { fetchDepartmentList, type DepartmentDto } from '@/api/department'

const router = useRouter()

const filter = reactive({
  name: '',
  departmentId: null as number | null
})

const staffList = ref<HisStaffDto[]>([])
const departmentList = ref<DepartmentDto[]>([])
const dialogVisible = ref(false)
const editingStaff = ref<HisStaffDto | null>(null)
const saving = ref(false)

const formData = reactive({
  staffCode: '',
  name: '',
  departmentId: null as number | null,
  title: '',
  phone: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  pageSizes: [10, 20, 50, 100]
})

const filteredList = computed(() => {
  return staffList.value.filter(staff => {
    if (filter.name && !staff.name.includes(filter.name)) return false
    if (filter.departmentId !== null && staff.departmentId !== filter.departmentId) return false
    return true
  })
})

const paginatedStaff = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  pagination.total = filteredList.value.length
  return filteredList.value.slice(start, end)
})

const handlePageChange = (page: number) => {
  pagination.page = page
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
}

const loadStaffList = async () => {
  try {
    const res = await fetchHisStaffList()
    if (res.code === 0) {
      staffList.value = res.data
    }
  } catch {
    ElMessage.error('加载人员列表失败')
  }
}

const loadDepartments = async () => {
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
  editingStaff.value = null
  formData.staffCode = ''
  formData.name = ''
  formData.departmentId = null
  formData.title = ''
  formData.phone = ''
  dialogVisible.value = true
}

const handleEdit = (staff: HisStaffDto) => {
  editingStaff.value = staff
  formData.staffCode = staff.staffCode
  formData.name = staff.name
  formData.departmentId = staff.departmentId
  formData.title = staff.title || ''
  formData.phone = staff.phone || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.staffCode || !formData.name) {
    ElMessage.warning('请填写工号和姓名')
    return
  }

  saving.value = true
  try {
    if (editingStaff.value) {
      const data: HisStaffUpdateRequest = {
        name: formData.name,
        departmentId: formData.departmentId || undefined,
        title: formData.title || undefined,
        phone: formData.phone || undefined
      }
      const res = await updateHisStaff(editingStaff.value.id, data)
      if (res.code === 0) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadStaffList()
      } else {
        ElMessage.error(res.message)
      }
    } else {
      const data: HisStaffCreateRequest = {
        staffCode: formData.staffCode,
        name: formData.name,
        departmentId: formData.departmentId || undefined,
        title: formData.title || undefined,
        phone: formData.phone || undefined
      }
      const res = await createHisStaff(data)
      if (res.code === 0) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadStaffList()
      } else {
        ElMessage.error(res.message)
      }
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (staff: HisStaffDto) => {
  ElMessageBox.confirm(`确定要删除人员"${staff.name}"吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteHisStaff(staff.id)
      if (res.code === 0) {
        ElMessage.success('删除成功')
        loadStaffList()
      } else {
        ElMessage.error(res.message)
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadStaffList()
  loadDepartments()
})
</script>

<style>
.his-staff-container {
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

.staff-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.name-text {
  font-weight: 500;
  color: var(--text-primary);
}

.no-data {
  color: var(--text-placeholder);
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
</style>