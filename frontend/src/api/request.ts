import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

function createService(): AxiosInstance {
  const service = axios.create({
    baseURL: '/api',
    timeout: 30000
  })

  service.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  service.interceptors.response.use(
    (response: AxiosResponse) => {
      const res = response.data as ApiResponse
      
      console.log('[request.ts] response:', response.config.url, 'data:', res)
      
      if (res.code !== 0) {
        const isLoginRequest = response.config.url?.includes('/auth/login')
        
        if (res.code === 401 && !isLoginRequest) {
          ElMessage.error(res.message || '登录已过期，请重新登录')
          const authStore = useAuthStore()
          authStore.logout()
          window.location.href = '/login'
          return Promise.reject(new Error(res.message || '未授权'))
        }
        
        if (!isLoginRequest) {
          ElMessage.error(res.message || '请求失败')
        }
        
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      
      return Promise.resolve(res)
    },
    (error) => {
      ElMessage.error(error.message || '网络错误')
      return Promise.reject(error)
    }
  )

  return service
}

const request = createService()

export default request
