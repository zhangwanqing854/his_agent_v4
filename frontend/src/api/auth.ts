import request from './request'
import type { ApiResponse } from './request'
import type { Duty } from '@/types/user'
import { users as mockUsers } from '@/mock/user'

export interface Department {
  id: number
  code: string
  name: string
  isPrimary: boolean
}

export interface LoginParams {
  usercode: string
  password: string
  captcha?: string
  captchaId?: string
}

export interface UserInfo {
  id: number
  hisStaffId: number | null
  username: string
  usercode: string
  name: string
  role: string
  avatar: string
  isSuperAdmin: boolean
  departments: Department[]
  duties?: Duty[]
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

const USE_MOCK = false

export function getCaptchaApi(): Promise<ApiResponse<any>> {
  return request.get('/auth/captcha') as Promise<ApiResponse<any>>
}

export async function loginApi(params: LoginParams): Promise<ApiResponse<LoginResponse>> {
  if (USE_MOCK) {
    const user = mockUsers.find(u =>
      u.usercode.toLowerCase() === params.usercode.toLowerCase() &&
      u.password === params.password
    )
    
    if (!user) {
      return {
        code: 1,
        message: '用户编码或密码错误',
        data: null as any
      }
    }
    
return {
        code: 0,
        message: '登录成功',
        data: {
          token: 'mock-token-' + user.id,
          userInfo: {
            id: user.id,
            hisStaffId: user.hisStaffId,
            username: user.username,
            usercode: user.usercode,
            name: user.username,
            role: user.role?.name || '',
            avatar: '',
            isSuperAdmin: user.isSuperAdmin,
            departments: [],
            duties: user.role?.duties || []
          }
        }
      }
  }
  return request.post('/auth/login', params) as Promise<ApiResponse<LoginResponse>>
}

export function logoutApi(): Promise<ApiResponse<void>> {
  return request.post('/auth/logout') as Promise<ApiResponse<void>>
}

export function getCurrentUserApi(): Promise<ApiResponse<UserInfo>> {
  return request.get('/auth/me') as Promise<ApiResponse<UserInfo>>
}