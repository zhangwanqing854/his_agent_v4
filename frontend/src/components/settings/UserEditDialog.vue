<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="600px"
    :close-on-click-modal="false"
    class="user-dialog"
    @closed="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="top"
      class="user-form"
    >
      <div class="form-section">
        <div class="section-title">
          <el-icon><User /></el-icon>
          基本信息
        </div>
        <el-form-item label="用户编码" prop="usercode">
          <el-input v-model="form.usercode" placeholder="请输入用户编码" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit && isSuperAdmin" />
          <div v-if="isEdit && isSuperAdmin" class="field-hint">超级管理员用户名不可修改</div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          <div v-if="isEdit" class="field-hint">留空表示不修改密码</div>
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="section-title">
          <el-icon><Key /></el-icon>
          权限配置
        </div>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" :disabled="isEdit && isSuperAdmin">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            >
              <span>{{ role.name }}</span>
              <span style="color: var(--text-secondary); font-size: 12px; margin-left: 8px">{{ role.description }}</span>
            </el-option>
          </el-select>
          <div v-if="isEdit && isSuperAdmin" class="field-hint">超级管理员角色不可修改</div>
        </el-form-item>
        
        <div v-if="selectedRole" class="duty-preview">
          <div class="preview-title">角色职责预览 ({{ selectedRole.duties?.length || 0 }} 个)</div>
          <div class="duty-tags">
            <el-tag
              v-for="duty in selectedRole.duties?.slice(0, 6)"
              :key="duty.id"
              effect="light"
              size="small"
              style="margin: 2px"
            >
              {{ duty.name }}
            </el-tag>
            <el-tag v-if="(selectedRole.duties?.length || 0) > 6" type="info" size="small" style="margin: 2px">
              +{{ (selectedRole.duties?.length || 0) - 6 }} 更多
            </el-tag>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="section-title">
          <el-icon><Link /></el-icon>
          HIS人员关联
        </div>
        <el-form-item label="关联HIS人员" prop="hisStaffId">
          <el-select
            v-model="form.hisStaffId"
            placeholder="请选择关联的HIS人员"
            clearable
            filterable
            :disabled="isEdit && isSuperAdmin"
          >
            <el-option
              v-for="staff in availableHisStaff"
              :key="staff.id"
              :label="`${staff.name} (${staff.staffCode})`"
              :value="staff.id"
            >
              <div class="staff-option">
                <span class="staff-name">{{ staff.name }}</span>
                <span class="staff-info">{{ staff.departmentName }} - {{ staff.title }}</span>
              </div>
            </el-option>
          </el-select>
          <div class="field-hint">关联后，用户将继承HIS人员的基本信息</div>
        </el-form-item>

        <div v-if="linkedStaff" class="staff-preview">
          <div class="preview-title">关联人员信息</div>
          <div class="staff-detail">
            <div class="detail-item">
              <span class="label">工号:</span>
              <span class="value">{{ linkedStaff.staffCode }}</span>
            </div>
            <div class="detail-item">
              <span class="label">姓名:</span>
              <span class="value">{{ linkedStaff.name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">科室:</span>
              <span class="value">{{ linkedStaff.departmentName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">职称:</span>
              <span class="value">{{ linkedStaff.title }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="section-title">
          <el-icon><Setting /></el-icon>
          状态设置
        </div>
        <el-form-item label="启用状态" prop="enabled">
          <el-radio-group v-model="form.enabled" :disabled="isEdit && isSuperAdmin">
            <el-radio-button :value="true">启用</el-radio-button>
            <el-radio-button :value="false">禁用</el-radio-button>
          </el-radio-group>
          <div v-if="isEdit && isSuperAdmin" class="field-hint">超级管理员不可禁用</div>
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Key, Link, Setting } from '@element-plus/icons-vue'
import {
  fetchRoleList,
  fetchHisStaffList,
  createUserApi,
  updateUserApi,
  checkUsercodeExists
} from '@/api/user'
import type { User as UserType, Role, HisStaff } from '@/types/user'

interface Props {
  modelValue: boolean
  user: UserType | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const roleList = ref<Role[]>([])
const allHisStaff = ref<HisStaff[]>([])

const isEdit = computed(() => !!props.user?.id)
const isSuperAdmin = computed(() => props.user?.isSuperAdmin)

const defaultForm = {
  usercode: '',
  username: '',
  password: '',
  roleId: 2,
  hisStaffId: null as number | null,
  enabled: true
}

const form = reactive({ ...defaultForm })

const selectedRole = computed(() => {
  return roleList.value.find(r => r.id === form.roleId)
})

const availableHisStaff = computed(() => {
  if (isEdit.value && props.user?.hisStaffId) {
    const currentStaff = allHisStaff.value.find(s => s.id === props.user?.hisStaffId)
    const unlinked = allHisStaff.value.filter(s => {
      const linkedIds = [props.user?.hisStaffId]
      return !linkedIds.includes(s.id)
    })
    return currentStaff ? [currentStaff, ...unlinked] : unlinked
  }
  return allHisStaff.value
})

const linkedStaff = computed(() => {
  if (!form.hisStaffId) return null
  return allHisStaff.value.find(s => s.id === form.hisStaffId)
})

const rules: FormRules = {
  usercode: [
    { required: true, message: '请输入用户编码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        const excludeId = isEdit.value ? props.user?.id : undefined
        if (checkUsercodeExists(value, excludeId)) {
          callback(new Error('用户编码已存在'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: !isEdit.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32个字符', trigger: 'blur' }
  ],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadData = async () => {
  try {
    const [roleRes, staffRes] = await Promise.all([
      fetchRoleList(),
      fetchHisStaffList()
    ])
    
    if (roleRes.code === 0) {
      roleList.value = roleRes.data
    }
    
    if (staffRes.code === 0) {
      allHisStaff.value = staffRes.data
    }
  } catch {
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadData()
})

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      loadData()
      if (props.user) {
        form.usercode = props.user.usercode
        form.username = props.user.username
        form.password = ''
        form.roleId = props.user.roleId
        form.hisStaffId = props.user.hisStaffId
        form.enabled = props.user.enabled
      } else {
        resetForm()
      }
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

const resetForm = () => {
  Object.assign(form, { ...defaultForm })
}

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let result
        if (isEdit.value && props.user?.id) {
          const updateData: Record<string, unknown> = {
            roleId: form.roleId,
            hisStaffId: form.hisStaffId,
            enabled: form.enabled
          }
          if (form.password) {
            updateData.password = form.password
          }
          if (form.username !== props.user?.username) {
            updateData.username = form.username
          }
          if (form.usercode !== props.user?.usercode) {
            updateData.usercode = form.usercode
          }
          result = await updateUserApi(props.user.id, updateData)
        } else {
          result = await createUserApi({
            usercode: form.usercode,
            username: form.username,
            password: form.password,
            roleId: form.roleId,
            hisStaffId: form.hisStaffId,
            enabled: form.enabled
          })
        }

        if (result.code === 0) {
          ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
          emit('success')
          handleClose()
        } else {
          ElMessage.error(result.message)
        }
      } catch {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style>
.user-dialog .el-dialog__body {
  padding: 16px 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.user-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title .el-icon {
  color: var(--color-primary-DEFAULT);
}

.field-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.duty-preview,
.staff-preview {
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  padding: 12px;
  margin-top: 12px;
}

.preview-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.duty-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.staff-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.staff-name {
  font-weight: 500;
}

.staff-info {
  font-size: 12px;
  color: var(--text-secondary);
}

.staff-detail {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.detail-item {
  display: flex;
  gap: 8px;
}

.detail-item .label {
  font-size: 13px;
  color: var(--text-secondary);
}

.detail-item .value {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>