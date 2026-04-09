import { defineStore } from 'pinia'
import { loginApi, logoutApi, getCurrentUserApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

export interface Department {
  id: number
  code: string
  name: string
  isPrimary: boolean
}

export interface UserInfo {
  id: number
  username: string
  name: string
  role: string
  avatar: string
  departments: Department[]
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserInfo | null,
    currentDepartmentId: Number(localStorage.getItem('currentDepartmentId')) || null as number | null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) => state.userInfo?.name || '',
    currentDepartment: (state) => {
      if (!state.userInfo?.departments || !state.currentDepartmentId) return null
      return state.userInfo.departments.find((d: Department) => d.id === state.currentDepartmentId) || null
    },
    currentDepartmentName: (state) => {
      if (!state.userInfo?.departments || !state.currentDepartmentId) return ''
      const dept = state.userInfo.departments.find((d: Department) => d.id === state.currentDepartmentId)
      return dept?.name || ''
    },
    canSwitchDepartment: (state) => (state.userInfo?.departments?.length ?? 0) > 1,
    userDepartments: (state) => state.userInfo?.departments || []
  },

  actions: {
    async login(usercode: string, password: string, captcha: string, captchaId: string): Promise<boolean> {
      try {
        console.log('[Auth] login called with:', { usercode })
        
        const response = await loginApi({ usercode, password, captcha, captchaId })
        console.log('[Auth] loginApi response:', response)
        
        const data = response as { code: number; message: string; data: { token: string; userInfo: UserInfo } | null }
        
        if (data.code !== 0) {
          throw new Error(data.message || '登录失败')
        }
        
        if (!data.data) {
          throw new Error('登录数据为空')
        }
        
        this.token = data.data.token
        localStorage.setItem('token', data.data.token)
        this.userInfo = data.data.userInfo
        
        if (this.userInfo?.departments?.length) {
          const primaryDept = this.userInfo.departments.find((d: Department) => d.isPrimary)
          const firstDept = this.userInfo.departments[0]
          this.currentDepartmentId = primaryDept?.id ?? firstDept?.id ?? null
          if (this.currentDepartmentId) {
            localStorage.setItem('currentDepartmentId', String(this.currentDepartmentId))
          }
        }
        
        console.log('[Auth] login success, token saved')
        return true
      } catch (error) {
        ElMessage.error('用户编码或密码错误')
        return false
      }
    },

    async fetchUserInfo() {
      const response = await getCurrentUserApi()
      const data = response as { code: number; data: UserInfo }
      this.userInfo = data.data
      
      if (!this.currentDepartmentId && this.userInfo?.departments?.length) {
        const primaryDept = this.userInfo.departments.find((d: Department) => d.isPrimary)
        const firstDept = this.userInfo.departments[0]
        this.currentDepartmentId = primaryDept?.id ?? firstDept?.id ?? null
        if (this.currentDepartmentId) {
          localStorage.setItem('currentDepartmentId', String(this.currentDepartmentId))
        }
      }
    },

    switchDepartment(departmentId: number) {
      if (!this.userInfo?.departments?.some((d: Department) => d.id === departmentId)) {
        console.warn('用户不属于该科室，无法切换')
        return
      }
      this.currentDepartmentId = departmentId
      localStorage.setItem('currentDepartmentId', String(departmentId))
    },

    async logout() {
      try {
        await logoutApi()
      } finally {
        this.token = ''
        this.userInfo = null
        this.currentDepartmentId = null
        localStorage.removeItem('token')
        localStorage.removeItem('currentDepartmentId')
      }
    }
  }
})
