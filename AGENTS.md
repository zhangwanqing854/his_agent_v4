# AGENTS.md

## Project Overview

医院医护智能交接班质量管理系统 - 北京大学国际医院

**Tech Stack:** Vue 3 + TypeScript + Vite (Frontend), Spring Boot 3 + JDK 17 (Backend - not yet implemented)

**Current Status:** Frontend with mock data. Backend pending.

---

## Commands

```bash
cd frontend
npm run dev          # Start dev server (http://localhost:3000)
npm run build        # Production build (type-check + build)
npm run type-check   # Run vue-tsc type checking
npm run lint         # Run oxlint + eslint with auto-fix
npm run format       # Format with Prettier
```

### Single File Type Check
```bash
cd frontend && npx vue-tsc --noEmit --pretty false 2>&1 | grep "specific-file.vue"
```

---

## Code Style Guidelines

### Imports (in order)
1. Vue/core: `import { ref, reactive } from 'vue'`
2. Third-party: `import { ElMessage } from 'element-plus'`
3. Local with @ alias: `import { useAuthStore } from '@/stores/auth'`

### TypeScript
- Use `interface` for objects, `type` for unions
- Avoid `any`; use `unknown` when uncertain
- Type-only imports: `import type { Foo } from 'module'`

### Vue Components
```vue
<script setup lang="ts">
interface Props { modelValue: boolean }
interface Emits { (e: 'update:modelValue', value: boolean): void }

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
</script>
```

### Naming Conventions
| Type | Convention | Example |
|------|------------|---------|
| Components | PascalCase | `CreateHandoverDialog.vue` |
| Utilities | camelCase | `auth.ts` |
| Variables/Functions | camelCase | `fetchPatients` |
| Constants | SCREAMING_SNAKE_CASE | `API_BASE_URL` |
| CSS classes | kebab-case | `.patient-table` |

### Error Handling
```typescript
try {
  await fetchData()
} catch (error) {
  ElMessage.error('加载失败')
} finally {
  loading.value = false
}
```

Never suppress errors silently: `catch (error) {}` ❌

---

## Project Structure

```
frontend/src/
├── api/           # API layer (axios wrapper + mock)
├── components/    # Reusable components
├── mock/          # Mock data (dev mode)
├── router/        # Vue Router config
├── stores/        # Pinia stores
├── styles/        # Global CSS (theme.css)
└── views/         # Page components
```

---

## Key Patterns

### Mock Data
```typescript
const USE_MOCK = true
export function fetchPatients() {
  if (USE_MOCK) return Promise.resolve({ code: 0, data: mockPatients })
  return request.get('/patients')
}
```

### Department Context
Doctors may work in multiple departments. Filter by `authStore.currentDepartmentId`:
```typescript
const filteredPatients = computed(() => 
  patients.filter(p => p.departmentId === authStore.currentDepartmentId)
)
```

### API Response Format
```typescript
interface ApiResponse<T> { code: number; message: string; data: T }
// code: 0 = success
```

---

## Formatting (Prettier)

```json
{ "semi": false, "singleQuote": true, "printWidth": 100 }
```

Run `npm run format` before committing.

---

## Theme

Primary: Light orange `#FFB366`. CSS variables in `theme.css`: `--color-primary-DEFAULT`, `--bg-primary`, `--text-primary`. Element Plus with Chinese locale (`zh-cn.mjs`).