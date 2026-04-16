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
  hisStaffId: number | null
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
    currentDepartmentId: Number(localStorage.getItem('currentDepartmentId')) || null as number | null,
    currentDepartmentCode: localStorage.getItem('currentDepartmentCode') || ''
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
async login(usercode: string, password: string): Promise<boolean> {
  try {
    console.log('[Auth] login called with:', { usercode })
    
    const response = await loginApi({ usercode, password })
    console.log('[Auth] loginApi response:', response)
    
    if (response.code !== 0) {
      throw new Error(response.message || '登录失败')
    }
    
    if (!response.data) {
      throw new Error('登录数据为空')
    }
    
    this.token = response.data.token
    localStorage.setItem('token', response.data.token)
    this.userInfo = response.data.userInfo
    
    if (this.userInfo?.departments?.length) {
      const primaryDept = this.userInfo.departments.find((d: Department) => d.isPrimary)
      const firstDept = this.userInfo.departments[0]
      this.currentDepartmentId = primaryDept?.id ?? firstDept?.id ?? null
      this.currentDepartmentCode = primaryDept?.code ?? firstDept?.code ?? ''
      
      if (this.currentDepartmentId) {
        localStorage.setItem('currentDepartmentId', String(this.currentDepartmentId))
      }
      if (this.currentDepartmentCode) {
        localStorage.setItem('currentDepartmentCode', this.currentDepartmentCode)
      }
    }
    
    console.log('[Auth] login success, token saved, deptId:', this.currentDepartmentId)
    return true
  } catch (error) {
    console.error('[Auth] login error:', error)
    ElMessage.error('用户编码或密码错误')
    return false
  }
},

async fetchUserInfo() {
  console.log('[Auth] fetchUserInfo called')
  try {
    const response = await getCurrentUserApi()
    console.log('[Auth] fetchUserInfo response:', response)
    
    if (response && response.code === 0 && response.data) {
      this.userInfo = response.data
      
      if (!this.currentDepartmentId && this.userInfo?.departments?.length) {
        const primaryDept = this.userInfo.departments.find((d: Department) => d.isPrimary)
        const firstDept = this.userInfo.departments[0]
        this.currentDepartmentId = primaryDept?.id ?? firstDept?.id ?? null
        this.currentDepartmentCode = primaryDept?.code ?? firstDept?.code ?? ''
        
        if (this.currentDepartmentId) {
          localStorage.setItem('currentDepartmentId', String(this.currentDepartmentId))
        }
        if (this.currentDepartmentCode) {
          localStorage.setItem('currentDepartmentCode', this.currentDepartmentCode)
        }
      }
      console.log('[Auth] fetchUserInfo done, currentDepartmentId:', this.currentDepartmentId)
    } else {
      console.error('[Auth] fetchUserInfo failed:', response)
    }
  } catch (e) {
    console.error('[Auth] fetchUserInfo error:', e)
  }
},

    switchDepartment(departmentId: number) {
      if (!this.userInfo?.departments?.some((d: Department) => d.id === departmentId)) {
        console.warn('用户不属于该科室，无法切换')
        return
      }
      
      const dept = this.userInfo?.departments?.find((d: Department) => d.id === departmentId)
      if (!dept) {
        console.warn('找不到科室信息')
        return
      }
      
      this.currentDepartmentId = departmentId
      this.currentDepartmentCode = dept.code
      localStorage.setItem('currentDepartmentId', String(departmentId))
      localStorage.setItem('currentDepartmentCode', dept.code)
      
      console.log('[Auth] switched to department:', dept.name, 'code:', dept.code)
    },

    async logout() {
      try {
        await logoutApi()
      } finally {
        this.token = ''
        this.userInfo = null
        this.currentDepartmentId = null
        this.currentDepartmentCode = ''
        localStorage.removeItem('token')
        localStorage.removeItem('currentDepartmentId')
        localStorage.removeItem('currentDepartmentCode')
      }
    }
  }
})
