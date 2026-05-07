import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const ROUTE_DUTY_MAP: Record<string, string> = {
  '/patients': 'PATIENT_MANAGEMENT',
  '/handovers': 'HANDOVER_MANAGEMENT',
  '/handovers/create': 'HANDOVER_MANAGEMENT',
  '/handovers/edit': 'HANDOVER_MANAGEMENT',
  '/todos': 'TODO_MANAGEMENT',
  '/statistics': 'STATISTICS_MANAGEMENT',
  '/users': 'USER_MANAGEMENT',
  '/roles': 'ROLE_MANAGEMENT',
  '/his-staff': 'STAFF_MANAGEMENT',
  '/departments': 'DEPARTMENT_MANAGEMENT',
  '/scheduling': 'SCHEDULING_MANAGEMENT',
  '/doctor-department': 'DOCTOR_DEPARTMENT_MANAGEMENT',
  '/settings': 'SYSTEM_SETTINGS',
  '/sms-config': 'SYSTEM_SETTINGS'
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/mobile-voice',
    name: 'MobileVoice',
    component: () => import('@/views/MobileVoiceRecord.vue'),
    meta: { title: '手机语音录入' }
  },
  {
    path: '/forbidden',
    name: 'Forbidden',
    component: () => import('@/views/Forbidden.vue'),
    meta: { title: '无权限访问' }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/patients',
    name: 'Patients',
    component: () => import('@/views/PatientManagement.vue'),
    meta: { title: '患者管理', requiresAuth: true, dutyCode: 'PATIENT_MANAGEMENT' }
  },
  {
    path: '/handovers',
    name: 'Handovers',
    component: () => import('@/views/HandoverManagement.vue'),
    meta: { title: '交班管理', requiresAuth: true, dutyCode: 'HANDOVER_MANAGEMENT' }
  },
  {
    path: '/handovers/create',
    name: 'CreateHandover',
    component: () => import('@/views/CreateHandoverPage.vue'),
    meta: { title: '发起交班', requiresAuth: true, dutyCode: 'HANDOVER_MANAGEMENT' }
  },
  {
    path: '/handovers/edit/:id',
    name: 'EditHandover',
    component: () => import('@/views/CreateHandoverPage.vue'),
    meta: { title: '编辑交班', requiresAuth: true, dutyCode: 'HANDOVER_MANAGEMENT' }
  },
  {
    path: '/todos',
    name: 'Todos',
    component: () => import('@/views/TodoList.vue'),
    meta: { title: '待办事项', requiresAuth: true, dutyCode: 'TODO_MANAGEMENT' }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/Statistics.vue'),
    meta: { title: '统计分析', requiresAuth: true, dutyCode: 'STATISTICS_MANAGEMENT' }
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import('@/views/UserManagement.vue'),
    meta: { title: '用户管理', requiresAuth: true, dutyCode: 'USER_MANAGEMENT' }
  },
  {
    path: '/roles',
    name: 'Roles',
    component: () => import('@/views/RoleManagement.vue'),
    meta: { title: '角色权限', requiresAuth: true, dutyCode: 'ROLE_MANAGEMENT' }
  },
  {
    path: '/his-staff',
    name: 'HisStaff',
    component: () => import('@/views/HisStaffManagement.vue'),
    meta: { title: '人员管理', requiresAuth: true, dutyCode: 'STAFF_MANAGEMENT' }
  },
  {
    path: '/departments',
    name: 'Departments',
    component: () => import('@/views/DepartmentManagement.vue'),
    meta: { title: '科室管理', requiresAuth: true, dutyCode: 'DEPARTMENT_MANAGEMENT' }
  },
  {
    path: '/scheduling',
    name: 'Scheduling',
    component: () => import('@/views/SchedulingManagement.vue'),
    meta: { title: '科室排班', requiresAuth: true, dutyCode: 'SCHEDULING_MANAGEMENT' }
  },
  {
    path: '/doctor-department',
    name: 'DoctorDepartment',
    component: () => import('@/views/DoctorDepartmentManagement.vue'),
    meta: { title: '科室人员管理', requiresAuth: true, dutyCode: 'DOCTOR_DEPARTMENT_MANAGEMENT' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SystemSettings.vue'),
    meta: { title: '系统设置', requiresAuth: true, dutyCode: 'SYSTEM_SETTINGS' }
  },
  {
    path: '/sms-config',
    name: 'SmsConfig',
    component: () => import('@/views/SmsConfigPage.vue'),
    meta: { title: '短信配置', requiresAuth: true, dutyCode: 'SYSTEM_SETTINGS' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  if (to.path === '/login' && token) {
    next('/')
    return
  }
  
  const dutyCode = to.meta.dutyCode as string | undefined
  if (dutyCode && token) {
    const authStore = useAuthStore()
    
    if (!authStore.userInfo) {
      try {
        await authStore.fetchUserInfo()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        localStorage.removeItem('token')
        next('/login')
        return
      }
    }
    
    if (!authStore.userInfo || !authStore.currentDepartmentId) {
      ElMessage.error('当前科室为空，请重新登录')
      localStorage.removeItem('token')
      next('/login')
      return
    }
    
    if (!authStore.hasDuty(dutyCode)) {
      next('/forbidden')
      return
    }
  }
  
  next()
})

export default router
export { ROUTE_DUTY_MAP }