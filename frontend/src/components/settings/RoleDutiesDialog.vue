<template>
  <el-dialog
    v-model="visible"
    :title="`${role?.name || ''} - 职责列表`"
    width="600px"
    class="duties-dialog"
  >
    <div class="duties-content">
      <div v-if="role?.duties?.length" class="duty-list">
        <div v-for="duty in role.duties" :key="duty.id" class="duty-item">
          <el-icon class="duty-icon"><Check /></el-icon>
          <div class="duty-info">
            <span class="duty-name">{{ duty.name }}</span>
            <span class="duty-code">{{ duty.code }}</span>
          </div>
        </div>
      </div>
      
      <el-empty v-if="!role?.duties?.length" description="暂无职责" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Check } from '@element-plus/icons-vue'
import type { Role } from '@/types/user'

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

.duty-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.duty-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.duty-icon {
  color: var(--color-success);
  margin-top: 2px;
}

.duty-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.duty-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.duty-code {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>