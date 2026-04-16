<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑角色' : '新增角色'"
    width="700px"
    :close-on-click-modal="false"
    class="role-dialog"
    @closed="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="top"
      class="role-form"
    >
      <div class="form-section">
        <div class="section-title">
          <el-icon><User /></el-icon>
          基本信息
        </div>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码（英文大写）" :disabled="isEdit && isPresetRole" />
          <div v-if="isEdit && isPresetRole" class="field-hint">预置角色编码不可修改</div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="默认角色" prop="isDefault">
          <el-switch v-model="form.isDefault" />
          <span class="switch-hint">新用户注册时默认分配此角色</span>
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="section-title">
          <el-icon><Key /></el-icon>
          职责分配
          <span class="section-hint">勾选此角色拥有的职责</span>
        </div>
        
        <div class="duty-assignment">
          <div v-for="permission in permissions" :key="permission.id" class="permission-group">
            <div class="permission-header">
              <el-checkbox
                :model-value="isPermissionAllSelected(permission.id)"
                :indeterminate="isPermissionIndeterminate(permission.id)"
                @change="(val: boolean) => handlePermissionSelectAll(permission.id, val)"
              >
                <span class="permission-name">{{ permission.name }}</span>
              </el-checkbox>
              <span class="permission-desc">{{ permission.description }}</span>
            </div>
            <div class="duty-list">
              <el-checkbox
                v-for="duty in getDutiesByPermission(permission.id)"
                :key="duty.id"
                v-model="form.dutyIds"
                :label="duty.id"
              >
                <span class="duty-name">{{ duty.name }}</span>
                <span class="duty-desc">{{ duty.description }}</span>
              </el-checkbox>
            </div>
          </div>
        </div>
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
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Key } from '@element-plus/icons-vue'
import { createRoleApi, updateRoleApi } from '@/api/user'
import type { Role, Permission, Duty, RoleFormData } from '@/types/user'

interface Props {
  modelValue: boolean
  role: Role | null
  permissions: Permission[]
  duties: Duty[]
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

const isEdit = computed(() => !!props.role?.id)
const isPresetRole = computed(() => props.role?.code && ['SUPER_ADMIN', 'DOCTOR'].includes(props.role.code))

const defaultForm: RoleFormData = {
  name: '',
  code: '',
  description: '',
  isDefault: false,
  dutyIds: []
}

const form = reactive<RoleFormData>({ ...defaultForm })

const rules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度为2-20个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

const getDutiesByPermission = (permissionId: number) => {
  return props.duties.filter(d => d.permissionId === permissionId)
}

const isPermissionAllSelected = (permissionId: number) => {
  const duties = getDutiesByPermission(permissionId)
  return duties.length > 0 && duties.every(d => form.dutyIds.includes(d.id))
}

const isPermissionIndeterminate = (permissionId: number) => {
  const duties = getDutiesByPermission(permissionId)
  const selectedCount = duties.filter(d => form.dutyIds.includes(d.id)).length
  return selectedCount > 0 && selectedCount < duties.length
}

const handlePermissionSelectAll = (permissionId: number, selected: boolean) => {
  const duties = getDutiesByPermission(permissionId)
  const dutyIds = duties.map(d => d.id)
  
  if (selected) {
    dutyIds.forEach(id => {
      if (!form.dutyIds.includes(id)) {
        form.dutyIds.push(id)
      }
    })
  } else {
    form.dutyIds = form.dutyIds.filter(id => !dutyIds.includes(id))
  }
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.role) {
      form.name = props.role.name
      form.code = props.role.code
      form.description = props.role.description
      form.isDefault = props.role.isDefault
      form.dutyIds = props.role.duties?.map(d => d.id) || []
    } else if (val) {
      Object.assign(form, { ...defaultForm, dutyIds: [] })
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    Object.assign(form, { ...defaultForm, dutyIds: [] })
  }
})

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
        if (isEdit.value && props.role?.id) {
          result = await updateRoleApi(props.role.id, form)
        } else {
          result = await createRoleApi(form)
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
.role-dialog .el-dialog__body {
  padding: 16px 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.role-form {
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

.section-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-secondary);
  margin-left: 8px;
}

.field-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.switch-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 8px;
}

.duty-assignment {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.permission-group {
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  padding: 12px;
}

.permission-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 8px;
}

.permission-name {
  font-weight: 500;
  color: var(--text-primary);
}

.permission-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.duty-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 24px;
}

.duty-list .el-checkbox {
  display: flex;
  align-items: flex-start;
  height: auto;
}

.duty-name {
  font-size: 14px;
  color: var(--text-primary);
}

.duty-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>