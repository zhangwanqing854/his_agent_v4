<template>
  <div class="department-container">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </div>
      <h1 class="page-title">
        <el-icon class="title-icon"><OfficeBuilding /></el-icon>
        科室管理
      </h1>
      <div class="header-right"></div>
    </div>

    <div class="content-wrapper">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input v-model="filter.name" placeholder="科室名称搜索" clearable style="width: 200px">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增科室
          </el-button>
        </div>
      </div>

      <el-table
        :data="filteredList"
        style="width: 100%"
        class="dept-table"
        :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
      >
        <el-table-column prop="code" label="科室编码" width="150" />
        <el-table-column prop="name" label="科室名称" min-width="200">
          <template #default="{ row }">
            <span class="name-text">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bedCount" label="床位数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.bedCount }} 张</el-tag>
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
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingDept ? '编辑科室' : '新增科室'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="80px">
        <el-form-item label="科室编码" required>
          <el-input v-model="formData.code" :disabled="!!editingDept" placeholder="请输入科室编码" />
        </el-form-item>
        <el-form-item label="科室名称" required>
          <el-input v-model="formData.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="床位数">
          <el-input-number v-model="formData.bedCount" :min="0" :max="9999" style="width: 100%" />
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
import { ArrowLeft, OfficeBuilding, Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import {
  fetchDepartmentList,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  type DepartmentDto
} from '@/api/department'

const router = useRouter()

const filter = reactive({
  name: ''
})

const deptList = ref<DepartmentDto[]>([])
const dialogVisible = ref(false)
const editingDept = ref<DepartmentDto | null>(null)
const saving = ref(false)

const formData = reactive({
  code: '',
  name: '',
  bedCount: 0
})

const filteredList = computed(() => {
  return deptList.value.filter(dept => {
    if (filter.name && !dept.name.includes(filter.name)) return false
    return true
  })
})

const loadDeptList = async () => {
  try {
    const res = await fetchDepartmentList()
    if (res.code === 0) {
      deptList.value = res.data
    }
  } catch {
    ElMessage.error('加载科室列表失败')
  }
}

const handleAdd = () => {
  editingDept.value = null
  formData.code = ''
  formData.name = ''
  formData.bedCount = 0
  dialogVisible.value = true
}

const handleEdit = (dept: DepartmentDto) => {
  editingDept.value = dept
  formData.code = dept.code
  formData.name = dept.name
  formData.bedCount = dept.bedCount
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.code || !formData.name) {
    ElMessage.warning('请填写科室编码和名称')
    return
  }

  saving.value = true
  try {
    if (editingDept.value) {
      const res = await updateDepartment(editingDept.value.id, {
        name: formData.name,
        bedCount: formData.bedCount
      })
      if (res.code === 0) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadDeptList()
      } else {
        ElMessage.error(res.message)
      }
    } else {
      const res = await createDepartment({
        code: formData.code,
        name: formData.name,
        bedCount: formData.bedCount
      })
      if (res.code === 0) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadDeptList()
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

const handleDelete = async (dept: DepartmentDto) => {
  ElMessageBox.confirm(`确定要删除科室"${dept.name}"吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDepartment(dept.id)
      if (res.code === 0) {
        ElMessage.success('删除成功')
        loadDeptList()
      } else {
        ElMessage.error(res.message)
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadDeptList()
})
</script>

<style>
.department-container {
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
  max-width: 1200px;
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

.dept-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.name-text {
  font-weight: 500;
  color: var(--text-primary);
}
</style>