<template>
  <el-dialog
    v-model="visible"
    :title="`${role?.name || ''} - 职责列表`"
    width="600px"
    class="duties-dialog"
  >
    <div class="duties-content">
      <div v-for="permission in permissionsWithDuties" :key="permission.id" class="permission-section">
        <div class="permission-title">
          <el-icon><Lock /></el-icon>
          {{ permission.name }}
          <el-tag size="small" effect="light">{{ permission.duties?.length || 0 }} 个职责</el-tag>
        </div>
        <div class="duty-items">
          <div v-for="duty in permission.duties" :key="duty.id" class="duty-item">
            <el-icon class="duty-icon"><Check /></el-icon>
            <div class="duty-info">
              <span class="duty-name">{{ duty.name }}</span>
              <span class="duty-desc">{{ duty.description }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-if="!role?.duties?.length" description="暂无职责" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Lock, Check } from '@element-plus/icons-vue'
import type { Role, Permission, Duty } from '@/types/user'

interface Props {
  modelValue: boolean
  role: Role | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)

const permissions = [
  { id: 1, code: 'HANDOVER', name: '交班管理' },
  { id: 2, code: 'PATIENT', name: '患者管理' },
  { id: 3, code: 'TODO', name: '待办事项' },
  { id: 4, code: 'STATISTICS', name: '统计分析' },
  { id: 5, code: 'USER_MANAGE', name: '用户管理' },
  { id: 6, code: 'ROLE_MANAGE', name: '角色权限' },
  { id: 7, code: 'SYSTEM_SETTINGS', name: '系统设置' }
]

const permissionsWithDuties = computed(() => {
  if (!props.role?.duties) return []
  
  return permissions.map(perm => ({
    ...perm,
    duties: props.role!.duties!.filter(d => d.permissionId === perm.id)
  })).filter(perm => perm.duties && perm.duties.length > 0)
})

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style>
.duties-dialog .el-dialog__body {
  padding: 16px 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.duties-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.permission-section {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 12px;
}

.permission-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
}

.permission-title .el-icon {
  color: var(--color-primary-DEFAULT);
}

.duty-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.duty-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px;
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
}

.duty-icon {
  color: var(--color-success);
  margin-top: 2px;
}

.duty-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.duty-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.duty-desc {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>