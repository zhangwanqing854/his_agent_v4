import { createRouter, createWebHistory } from 'vue-router'

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
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/patients',
    name: 'Patients',
    component: () => import('@/views/PatientManagement.vue'),
    meta: { title: '患者管理', requiresAuth: true }
  },
  {
    path: '/handovers',
    name: 'Handovers',
    component: () => import('@/views/HandoverManagement.vue'),
    meta: { title: '交班管理', requiresAuth: true }
  },
  {
    path: '/handovers/create',
    name: 'CreateHandover',
    component: () => import('@/views/CreateHandoverPage.vue'),
    meta: { title: '发起交班', requiresAuth: true }
  },
  {
    path: '/handovers/edit/:id',
    name: 'EditHandover',
    component: () => import('@/views/CreateHandoverPage.vue'),
    meta: { title: '编辑交班', requiresAuth: true }
  },
  {
    path: '/todos',
    name: 'Todos',
    component: () => import('@/views/TodoList.vue'),
    meta: { title: '待办事项', requiresAuth: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/Statistics.vue'),
    meta: { title: '统计分析', requiresAuth: true }
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import('@/views/UserManagement.vue'),
    meta: { title: '用户管理', requiresAuth: true }
  },
  {
    path: '/roles',
    name: 'Roles',
    component: () => import('@/views/RoleManagement.vue'),
    meta: { title: '角色权限', requiresAuth: true }
  },
  {
    path: '/his-staff',
    name: 'HisStaff',
    component: () => import('@/views/HisStaffManagement.vue'),
    meta: { title: '人员管理', requiresAuth: true }
  },
  {
    path: '/departments',
    name: 'Departments',
    component: () => import('@/views/DepartmentManagement.vue'),
    meta: { title: '科室管理', requiresAuth: true }
  },
  {
    path: '/scheduling',
    name: 'Scheduling',
    component: () => import('@/views/SchedulingManagement.vue'),
    meta: { title: '科室排班', requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SystemSettings.vue'),
    meta: { title: '系统设置', requiresAuth: true }
  },
  {
    path: '/sms-config',
    name: 'SmsConfig',
    component: () => import('@/views/SmsConfigPage.vue'),
    meta: { title: '短信配置', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
